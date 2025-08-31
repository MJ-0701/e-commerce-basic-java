package com.example.allrabackendassignment.service;

import com.example.allrabackendassignment.domain.order.entity.GeneratedUUID;
import com.example.allrabackendassignment.domain.order.entity.OrderHistories;
import com.example.allrabackendassignment.domain.order.entity.OrderItems;
import com.example.allrabackendassignment.domain.order.entity.Orders;
import com.example.allrabackendassignment.domain.order.repository.OrderHistoriesRepository;
import com.example.allrabackendassignment.domain.order.repository.OrderItemsRepository;
import com.example.allrabackendassignment.domain.order.repository.OrdersRepository;
import com.example.allrabackendassignment.domain.product.entity.Product;
import com.example.allrabackendassignment.domain.product.repository.ProductRepository;
import com.example.allrabackendassignment.event.OrderCreatedEvent;
import com.example.allrabackendassignment.global.http.exception.InsufficientStockException;
import com.example.allrabackendassignment.web.dto.internal.order.request.OrderItemRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@org.junit.jupiter.api.extension.ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrdersRepository ordersRepository;
    @Mock private OrderHistoriesRepository orderHistoriesRepository;
    @Mock private OrderItemsRepository orderItemsRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private OrderService orderService;

    private Orders savedOrdersMock;

    @BeforeEach
    void setUp() {
        savedOrdersMock = mock(Orders.class);
        when(savedOrdersMock.getId()).thenReturn(10L);

        // ✅ 타입에 맞게 GeneratedUUID 객체 리턴
        when(savedOrdersMock.getOrderId()).thenReturn(new GeneratedUUID("OID-TEST"));

        when(savedOrdersMock.getPaidAmount()).thenReturn(3000);
        when(ordersRepository.save(any(Orders.class))).thenReturn(savedOrdersMock);

        when(orderHistoriesRepository.save(any(OrderHistories.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(orderItemsRepository.saveAll(anyList()))
                .thenAnswer(inv -> inv.getArgument(0));
    }
    private OrderItemRequest makeOrderRequest(List<OrderItemRequest.CartItemRequest> items) {
        return new OrderItemRequest(items); // ← 레코드가 items 하나만 받음
    }

    private Product makeActiveProduct(long id, int stock) {
        return Product.builder()
                .productId(id)
                .productName("상품#" + id)
                .productPrice(1000)
                .isActive(true)
                .stock(stock)
                .build();
    }

    @Nested
    @DisplayName("createOrder - 성공")
    class Success {

        @Test
        @DisplayName("중복 상품 수량 집계 → 재고 차감 → 주문/아이템/히스토리 저장 → 이벤트 발행")
        void createOrder_success() {
            // given: 같은 상품(1L)이 1개 + 2개 = 총 3개
            var items = List.of(
                    new OrderItemRequest.CartItemRequest(1L, "상품A", 1, 1000, 1000),
                    new OrderItemRequest.CartItemRequest(1L, "상품A", 2, 1000, 2000)
            );
            var req = makeOrderRequest(items);
            assertEquals(3000, req.totalOrderPrice());

            Product p1 = makeActiveProduct(1L, 10);
            when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(p1));

            // when
            orderService.createOrder(req);

            // then: 재고 10 → 7 (총 3개 차감)
            assertEquals(7, p1.getStock());

            // 비관적 락 조회는 productId 1번만 호출 (집계 후 한 번)
            verify(productRepository, times(1)).findByIdForUpdate(1L);

            // 저장 호출 검증
            verify(ordersRepository, times(1)).save(any(Orders.class));
            ArgumentCaptor<List<OrderItems>> itemsCaptor = ArgumentCaptor.forClass(List.class);
            verify(orderItemsRepository, times(1)).saveAll(itemsCaptor.capture());
            assertEquals(2, itemsCaptor.getValue().size(), "원본 카트 라인 수만큼 저장");

            verify(orderHistoriesRepository, times(1)).save(any(OrderHistories.class));

            // 이벤트 발행 검증
            ArgumentCaptor<OrderCreatedEvent> evtCaptor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
            verify(eventPublisher, times(1)).publishEvent(evtCaptor.capture());
            OrderCreatedEvent evt = evtCaptor.getValue();
            assertEquals("OID-TEST", evt.orderPublicId());
            assertEquals(3000, evt.paidAmount()); // int 가정
        }
    }

    @Nested
    @DisplayName("createOrder - 실패/롤백")
    class Failure {

        @Test
        @DisplayName("재고 부족 → InsufficientStockException, 저장/이벤트 호출 없음")
        void insufficientStock_throws() {
            var items = List.of(
                    new OrderItemRequest.CartItemRequest(1L, "상품A", 5, 1000, 5000)
            );
            var req = makeOrderRequest(items);

            Product p1 = makeActiveProduct(1L, 3);
            when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(p1));

            assertThrows(InsufficientStockException.class, () -> orderService.createOrder(req));

            verify(ordersRepository, never()).save(any());
            verify(orderHistoriesRepository, never()).save(any());
            verify(orderItemsRepository, never()).saveAll(anyList());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("비활성 상품 → IllegalStateException")
        void inactiveProduct_throws() {
            var items = List.of(
                    new OrderItemRequest.CartItemRequest(2L, "상품B", 1, 1000, 1000)
            );
            var req = makeOrderRequest(items);

            Product inactive = Product.builder()
                    .productId(2L)
                    .productName("상품B")
                    .productPrice(1000)
                    .isActive(false)
                    .stock(100)
                    .build();
            when(productRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(inactive));

            assertThrows(IllegalStateException.class, () -> orderService.createOrder(req));

            verify(ordersRepository, never()).save(any());
            verify(orderHistoriesRepository, never()).save(any());
            verify(orderItemsRepository, never()).saveAll(anyList());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("상품 미존재 → IllegalArgumentException")
        void productNotFound_throws() {
            var items = List.of(
                    new OrderItemRequest.CartItemRequest(99L, "없는상품", 1, 1000, 1000)
            );
            var req = makeOrderRequest(items);

            when(productRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(req));

            verify(ordersRepository, never()).save(any());
            verify(orderHistoriesRepository, never()).save(any());
            verify(orderItemsRepository, never()).saveAll(anyList());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }
}