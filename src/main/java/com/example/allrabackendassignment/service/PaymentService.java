package com.example.allrabackendassignment.service;

import com.example.allrabackendassignment.domain.payment.entity.PaymentStatus;
import com.example.allrabackendassignment.domain.payment.entity.PaymentTransaction;
import com.example.allrabackendassignment.domain.payment.entity.PaymentTxHistories;
import com.example.allrabackendassignment.domain.payment.repository.PaymentCancellationsRepository;
import com.example.allrabackendassignment.domain.payment.repository.PaymentTransactionRepository;
import com.example.allrabackendassignment.domain.payment.repository.PaymentTxHistoriesRepository;
import com.example.allrabackendassignment.external.PaymentFeignClient;
import com.example.allrabackendassignment.global.config.RabbitConfig;
import com.example.allrabackendassignment.web.dto.external.PaymentCommand;
import com.example.allrabackendassignment.web.dto.external.PaymentRequest;
import com.example.allrabackendassignment.web.dto.external.PaymentResponse;
import com.example.allrabackendassignment.web.dto.external.PaymentResultMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final RabbitTemplate rabbitTemplate;
    private final PaymentFeignClient paymentFeignClient;
    private final PaymentTransactionRepository txRepository;
    private final PaymentTxHistoriesRepository historyRepository;
    private final PaymentCancellationsRepository cancelRepository;

    // correlationId → waiter
    private final Map<String, CompletableFuture<PaymentResultMessage>> waiters = new ConcurrentHashMap<>();

    private static final int    MAX_ATTEMPTS = 6;                 // 10s × 6
    private static final long   RETRY_SPAN_SECONDS = 10L * MAX_ATTEMPTS; // 60s
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(RETRY_SPAN_SECONDS + 15); // 여유 15s

    /**
     * 주문 커밋 이후 호출되는 동기 메서드.
     */
    public PaymentResultMessage processPaymentWithRetrySync(Long orderPk, String orderId, int amount, PaymentRequest request) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<PaymentResultMessage> fut = new CompletableFuture<>();
        // 타임아웃을 Future에도 직접 건다 (추가 안전장치)
        fut.orTimeout(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
        waiters.put(correlationId, fut);

        PaymentCommand cmd = new PaymentCommand(correlationId, orderPk, orderId, 1, MAX_ATTEMPTS, amount, request);
        rabbitTemplate.convertAndSend(RabbitConfig.PAYMENT_EXCHANGE, RabbitConfig.RK_PAYMENT_REQUEST, cmd);

        try {
            return fut.get(WAIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            return PaymentResultMessage.failure(correlationId, orderPk, orderId, "PAYMENT_TIMEOUT");
        } catch (Exception e) {
            return PaymentResultMessage.failure(correlationId, orderPk, orderId, "PAYMENT_INTERNAL_ERROR");
        } finally {
            waiters.remove(correlationId);
        }
    }

    // 재시도 판단 util
    private boolean isRetryableStatus(int status) {
        // 408/429/5xx만 재시도
        return status == 408 || status == 429 || status >= 500;
    }

    @RabbitListener(queues = RabbitConfig.PAYMENT_REQUEST_QUEUE)
    public void onPaymentRequest(PaymentCommand cmd) {
        try {
            PaymentResponse resp = paymentFeignClient.requestPayment(cmd.request());
            boolean ok = resp != null && "SUCCESS".equalsIgnoreCase(String.valueOf(resp.status()));
            if (ok) {
                rabbitTemplate.convertAndSend(RabbitConfig.PAYMENT_EXCHANGE, RabbitConfig.RK_PAYMENT_RESULT,
                        PaymentResultMessage.success(cmd.correlationId(), cmd.orderPk(), cmd.orderId(), resp));
                return;
            }
            // 응답은 왔지만 실패(예: status="FAILED") → 재시도 비대상으로 처리
            rabbitTemplate.convertAndSend(RabbitConfig.PAYMENT_EXCHANGE, RabbitConfig.RK_PAYMENT_RESULT,
                    PaymentResultMessage.failure(cmd.correlationId(), cmd.orderPk(), cmd.orderId(), "PG_FAILED"));
        } catch (FeignException fe) {
            int status = fe.status();
            String reason = fe.contentUTF8(); // PG 원문
            if (isRetryableStatus(status) && cmd.attempt() < cmd.maxAttempts()) {
                PaymentCommand next = new PaymentCommand(cmd.correlationId(), cmd.orderPk(), cmd.orderId(),
                        cmd.attempt() + 1, cmd.maxAttempts(), cmd.amount(), cmd.request());
                rabbitTemplate.convertAndSend(RabbitConfig.PAYMENT_RETRY_EXCHANGE, RabbitConfig.RK_PAYMENT_REQUEST, next);
            } else {
                // 400 같은 경우 여기로: 즉시 최종실패
                rabbitTemplate.convertAndSend(RabbitConfig.PAYMENT_EXCHANGE, RabbitConfig.RK_PAYMENT_RESULT,
                        PaymentResultMessage.failure(cmd.correlationId(), cmd.orderPk(), cmd.orderId(),
                                (reason == null || reason.isBlank()) ? ("HTTP_" + status) : reason));
            }
        } catch (Exception ex) {
            // 네트워크 등 알 수 없는 오류는 재시도
            if (cmd.attempt() < cmd.maxAttempts()) {
                PaymentCommand next = new PaymentCommand(cmd.correlationId(), cmd.orderPk(), cmd.orderId(),
                        cmd.attempt() + 1, cmd.maxAttempts(), cmd.amount(), cmd.request());
                rabbitTemplate.convertAndSend(RabbitConfig.PAYMENT_RETRY_EXCHANGE, RabbitConfig.RK_PAYMENT_REQUEST, next);
            } else {
                rabbitTemplate.convertAndSend(RabbitConfig.PAYMENT_EXCHANGE, RabbitConfig.RK_PAYMENT_RESULT,
                        PaymentResultMessage.failure(cmd.correlationId(), cmd.orderPk(), cmd.orderId(), "EXHAUSTED_RETRY"));
            }
        }
    }

    /**
     * 결과 Consumer: 대기 중인 호출 완료.
     * 타임아웃 뒤 늦게 도착한 결과는 로그만 남기고 무시.
     */
    @RabbitListener(queues = RabbitConfig.PAYMENT_RESULT_QUEUE)
    public void onPaymentResult(PaymentResultMessage result) {
        CompletableFuture<PaymentResultMessage> fut = waiters.get(result.correlationId());
        if (fut != null) {
            fut.complete(result);
        } else {
            // 타임아웃 등으로 이미 제거된 경우
            // log.warn("Late payment result: {}", result);
        }
    }

    // 기존 단순 동기 호출이 필요하면 유지
    public PaymentResponse requestPayment(PaymentRequest request){
        return paymentFeignClient.requestPayment(request);
    }

    @Transactional
    public PaymentTransaction createPaymentTransaction(Long orderPk, int amount, PaymentRequest req) {
        PaymentTransaction tx = PaymentTransaction.builder()
                .orderId(orderPk)
                .amount(amount)
                .pgCode("credit_card") // TODO: req 안에 결제수단 정보 있으면 거기서 세팅
                .createdAt(LocalDateTime.now())
                .build();
        txRepository.save(tx);

        // 최초 요청 이력 기록
        historyRepository.save(PaymentTxHistories.builder()
                .transactionId(tx.getId())
                .status(PaymentStatus.REQUEST)
                .build());

        return tx;
    }

    @Transactional
    public String markTransactionCompleted(PaymentTransaction tx, PaymentResponse resp) {
        tx.updateTid(resp.transactionId()); // PG사 Transaction ID
        txRepository.save(tx);

        historyRepository.save(PaymentTxHistories.builder()
                .transactionId(tx.getId())
                .status(PaymentStatus.COMPLETED)
                .build());

        return resp.transactionId();
    }

    @Transactional
    public void markTransactionFailed(PaymentTransaction tx, String reason) {
        historyRepository.save(PaymentTxHistories.builder()
                .transactionId(tx.getId())
                .status(PaymentStatus.FAILED)
                .build());
    }

    private String toJson(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}