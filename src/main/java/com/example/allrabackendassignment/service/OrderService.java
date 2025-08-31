package com.example.allrabackendassignment.service;

import com.example.allrabackendassignment.domain.order.entity.Orders;
import com.example.allrabackendassignment.domain.payment.entity.PaymentTransaction;
import com.example.allrabackendassignment.global.http.ErrorCode;
import com.example.allrabackendassignment.global.http.exception.BusinessException;
import com.example.allrabackendassignment.web.dto.external.PaymentRequest;
import com.example.allrabackendassignment.web.dto.external.PaymentResultMessage;
import com.example.allrabackendassignment.web.dto.internal.order.request.OrderItemRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 오케스트레이션 전용 서비스 (비트랜잭션):
 * 1) OrderTxService.createOrderRecords() 호출 → 커밋 보장
 * 2) PaymentService.processPaymentWithRetrySync() 호출 (동기, 재시도 포함)
 * 3) 결과에 따라 OrderTxService.markOrderPaid/Failed() (REQUIRES_NEW로 각각 커밋)
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderTxService orderTxService;
    private final PaymentService paymentService;

    public OrderCreateResponse createOrderOrchestrator(OrderItemRequest orderItemRequest) {
        // 1) 주문 생성 트랜잭션 커밋
        Orders created = orderTxService.createOrderRecords(orderItemRequest);

        // 2) 결제 요청 본문 구성
        PaymentRequest paymentReq = new PaymentRequest(
                created.getOrderId().toString(),
                created.getPaidAmount()
        );

        // 2-1) 결제 트랜잭션 레코드 생성 + 최초 REQUEST 이력 기록
        var paymentTx = paymentService.createPaymentTransaction(
                created.getId(),
                created.getPaidAmount(),
                paymentReq
        );

        // 2-2) 결제 처리 (동기 + RabbitMQ 재시도 정책 적용)
        PaymentResultMessage result;
        try {
            result = paymentService.processPaymentWithRetrySync(
                    created.getId(),
                    created.getOrderId().toString(),
                    created.getPaidAmount(),
                    paymentReq
            );
        } catch (Exception e) {
            // 예외 상황도 실패로 간주하여 동일한 후처리 + 비즈니스 예외로 변환
            paymentService.markTransactionFailed(paymentTx, "PAYMENT_INTERNAL_ERROR");
            orderTxService.markOrderPaymentFailed(created.getId(), "PAYMENT_INTERNAL_ERROR");
            orderTxService.compensateOrderCreation(created.getId());
            throw new BusinessException(ErrorCode.PAYMENT_GATEWAY_ERROR, "결제 처리 중 내부 오류");
        }

        // 3) 결과에 따른 상태 업데이트 (각각 독립 커밋)
        if (result.success()) {
            // ★ 성공: 트랜잭션에 TID 반영하고 반환값에도 포함
            String tid = paymentService.markTransactionCompleted(paymentTx, result.response());

            // (원래 로직 유지) 주문 상태 COMPLETED 처리
            orderTxService.markOrderPaid(created.getId());

            return new OrderCreateResponse(created.getOrderId().toString(), true, null, tid);
        } else {
            // (원래 로직 유지) 실패 로그 & 주문 실패 처리
            paymentService.markTransactionFailed(paymentTx, result.failureReason());
            orderTxService.markOrderPaymentFailed(created.getId(), result.failureReason());
            // 보상 트랜잭션: 재고 원복 + 주문 CANCELED
            orderTxService.compensateOrderCreation(created.getId());

            // ✅ 여기서 200을 리턴하지 않고 비즈니스 예외로 변환해 던짐
            throw new BusinessException(mapPaymentFailure(result.failureReason()), result.failureReason());
        }
    }

    /** PG/재시도 사유를 프로젝트의 ErrorCode로 매핑 */
    private ErrorCode mapPaymentFailure(String reason) {
        if (reason == null) return ErrorCode.PAYMENT_FAILED;
        String r = reason.toUpperCase();

        // 재시도 소진/타임아웃류
        if (r.contains("TIMEOUT") || r.contains("EXHAUSTED_RETRY")) return ErrorCode.PAYMENT_TIMEOUT;

        // PG 400류
        if (r.startsWith("HTTP_400") || r.contains("BAD_REQUEST")) return ErrorCode.PG_BAD_REQUEST;

        // PG/통신 오류류(HTTP_5xx 등)
        if (r.startsWith("HTTP_") || r.contains("PG") || r.contains("GATEWAY")) return ErrorCode.PAYMENT_GATEWAY_ERROR;

        // 그 외 일반 실패
        return ErrorCode.PAYMENT_FAILED;
    }

    // 유틸
    public static String generateOrderName(java.util.List<com.example.allrabackendassignment.web.dto.internal.order.request.OrderItemRequest.CartItemRequest> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) return "상품 없음";
        if (orderItems.size() == 1) return orderItems.get(0).productName();
        return orderItems.get(0).productName() + " 외 " + (orderItems.size() - 1) + "개";
    }

    public record OrderCreateResponse(String orderId, boolean paymentSuccess, String failureReason, String transactionId ) {}
}