package com.example.allrabackendassignment.service;

import com.example.allrabackendassignment.domain.order.entity.GeneratedUUID;
import com.example.allrabackendassignment.domain.order.entity.Orders;
import com.example.allrabackendassignment.web.dto.external.PaymentRequest;
import com.example.allrabackendassignment.web.dto.external.PaymentResultMessage;
import com.example.allrabackendassignment.web.dto.internal.order.request.OrderItemRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderTxService orderTxService;
    @Mock private PaymentService paymentService;

    @InjectMocks private OrderService orderService;

    @Captor private ArgumentCaptor<Long> orderPkCaptor;
    @Captor private ArgumentCaptor<String> orderIdCaptor;
    @Captor private ArgumentCaptor<Integer> amountCaptor;
    @Captor private ArgumentCaptor<PaymentRequest> paymentRequestCaptor;

    private Orders createdOrderMock;
    private Long orderPk;
    private String orderId;
    private int paidAmount;

    @BeforeEach
    void setUp() {
        // Orders는 엔티티이므로 Mockito mock으로 안전하게 스텁
        createdOrderMock = mock(Orders.class);
        orderPk = 123L;
        orderId = "OID" +  UUID.randomUUID().toString();
        paidAmount = 42_000;

        when(createdOrderMock.getId()).thenReturn(orderPk);
        when(createdOrderMock.getOrderId()).thenReturn(new GeneratedUUID(orderId));
        when(createdOrderMock.getPaidAmount()).thenReturn(paidAmount);
    }

    @Nested
    class CreateOrderOrchestrator {

        @Test
        @DisplayName("결제 성공 시: markOrderPaid 호출 및 성공 응답 반환")
        void successFlow() {
            // given
            OrderItemRequest req = mock(OrderItemRequest.class);

            when(orderTxService.createOrderRecords(req))
                    .thenReturn(createdOrderMock);

            // PaymentService가 성공 결과를 돌려주도록 스텁
            PaymentResultMessage success =
                    PaymentResultMessage.success("corr", orderPk, orderId.toString(), /*response*/ null);
            when(paymentService.processPaymentWithRetrySync(
                    anyLong(), anyString(), anyInt(), any(PaymentRequest.class))
            ).thenReturn(success);

            // when
            OrderService.OrderCreateResponse resp = orderService.createOrderOrchestrator(req);

            // then
            assertThat(resp.orderId()).isEqualTo(orderId.toString());
            assertThat(resp.paymentSuccess()).isTrue();
            assertThat(resp.failureReason()).isNull();

            // 결제 호출 파라미터 검증
            verify(paymentService).processPaymentWithRetrySync(
                    orderPkCaptor.capture(),
                    orderIdCaptor.capture(),
                    amountCaptor.capture(),
                    paymentRequestCaptor.capture()
            );
            assertThat(orderPkCaptor.getValue()).isEqualTo(orderPk);
            assertThat(orderIdCaptor.getValue()).isEqualTo(orderId.toString());
            assertThat(amountCaptor.getValue()).isEqualTo(paidAmount);

            PaymentRequest sentReq = paymentRequestCaptor.getValue();
            assertThat(sentReq.orderId()).isEqualTo(orderId.toString());
            assertThat(sentReq.amount()).isEqualTo(paidAmount);

            // 상태 변경은 성공 분기만 호출
            verify(orderTxService, times(1)).markOrderPaid(orderPk);
            verify(orderTxService, never()).markOrderPaymentFailed(anyLong(), anyString());
        }

        @Test
        @DisplayName("결제 실패 시: markOrderPaymentFailed 호출 및 실패 응답 반환")
        void failureFlow() {
            // given
            OrderItemRequest req = mock(OrderItemRequest.class);

            when(orderTxService.createOrderRecords(req))
                    .thenReturn(createdOrderMock);

            PaymentResultMessage failure =
                    PaymentResultMessage.failure("corr", orderPk, orderId.toString(), "PAYMENT_FINAL_FAILURE");
            when(paymentService.processPaymentWithRetrySync(
                    anyLong(), anyString(), anyInt(), any(PaymentRequest.class))
            ).thenReturn(failure);

            // when
            OrderService.OrderCreateResponse resp = orderService.createOrderOrchestrator(req);

            // then
            assertThat(resp.orderId()).isEqualTo(orderId.toString());
            assertThat(resp.paymentSuccess()).isFalse();
            assertThat(resp.failureReason()).isEqualTo("PAYMENT_FINAL_FAILURE");

            verify(orderTxService, times(1))
                    .markOrderPaymentFailed(eq(orderPk), eq("PAYMENT_FINAL_FAILURE"));
            verify(orderTxService, never()).markOrderPaid(anyLong());
        }
    }

    @Nested
    class GenerateOrderNameTest {

        @Test
        @DisplayName("주문명: 아이템이 비어있으면 '상품 없음'")
        void emptyItems() {
            assertThat(OrderService.generateOrderName(null)).isEqualTo("상품 없음");
            assertThat(OrderService.generateOrderName(java.util.List.of())).isEqualTo("상품 없음");
        }

        @Test
        @DisplayName("주문명: 단일 아이템이면 해당 상품명")
        void singleItem() {
            var item = new OrderItemRequest.CartItemRequest(1L, "티셔츠", 1, 1000, 1000);
            assertThat(OrderService.generateOrderName(java.util.List.of(item))).isEqualTo("티셔츠");
        }

        @Test
        @DisplayName("주문명: 2개 이상이면 '첫상품 외 n개'")
        void multiItems() {
            var i1 = new OrderItemRequest.CartItemRequest(1L, "티셔츠", 1, 1000, 1000);
            var i2 = new OrderItemRequest.CartItemRequest(2L, "바지", 1, 2000, 2000);
            var i3 = new OrderItemRequest.CartItemRequest(3L, "모자", 1, 3000, 3000);
            assertThat(OrderService.generateOrderName(java.util.List.of(i1, i2, i3)))
                    .isEqualTo("티셔츠 외 2개");
        }
    }
}