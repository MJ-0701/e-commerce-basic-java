package com.example.allrabackendassignment.event;

import com.example.allrabackendassignment.service.PaymentService;
import com.example.allrabackendassignment.web.dto.external.PaymentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentAfterCommitListener {

    private final PaymentService paymentService;

    /**
     * 주문 트랜잭션이 "정상 커밋"된 경우에만 호출됨.
     * 별도 트랜잭션에서 실행되도록 분리(결제 로그 적재 등 필요한 경우 REQUIRES_NEW 권장).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Order committed. Trigger payment. orderId={}, amount={}",
                event.orderPublicId(), event.paidAmount());

        PaymentRequest paymentRequest = new PaymentRequest(
                event.orderPublicId(),
                event.paidAmount()
        );

        // 외부 PG 요청
        paymentService.requestPayment(paymentRequest);
    }
}
