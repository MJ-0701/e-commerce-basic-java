package com.example.allrabackendassignment.service;

import com.example.allrabackendassignment.domain.order.entity.*;
import com.example.allrabackendassignment.domain.order.repository.OrderHistoriesRepository;
import com.example.allrabackendassignment.domain.order.repository.OrderItemsRepository;
import com.example.allrabackendassignment.domain.order.repository.OrdersRepository;
import com.example.allrabackendassignment.web.dto.internal.order.request.OrderItemRequest;
import com.example.allrabackendassignment.web.dto.internal.order.request.OrderItemRequest.CartItemRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrdersRepository ordersRepository;
    @Mock private OrderHistoriesRepository orderHistoriesRepository;
    @Mock private OrderItemsRepository orderItemsRepository;

    @InjectMocks private OrderService orderService;

    @Captor private ArgumentCaptor<Orders> ordersCaptor;
    @Captor private ArgumentCaptor<OrderHistories> historiesCaptor;
    @Captor private ArgumentCaptor<List<OrderItems>> orderItemsListCaptor;

    @BeforeEach
    void setUp() {
        // ordersRepository.save(..) 호출 시, id가 채워진 영속 엔티티를 반환하도록 셋업
        lenient().when(ordersRepository.save(any(Orders.class)))
                .thenAnswer(invocation -> {
                    Orders in = invocation.getArgument(0, Orders.class);
                    return Orders.builder()
                            .id(1L) // 생성된 PK 가정
                            .orderId(in.getOrderId())
                            .orderName(in.getOrderName())
                            .userName(in.getUserName())
                            .userId(in.getUserId())
                            .orderStatus(in.getOrderStatus())
                            .totalAmount(in.getTotalAmount())
                            .paidAmount(in.getPaidAmount())
                            .canceledAmount(in.getCanceledAmount())
                            .isFullyCanceled(in.isFullyCanceled())
                            .build();
                });

        lenient().when(orderHistoriesRepository.save(any(OrderHistories.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        lenient().when(orderItemsRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("주문 생성: orders 저장, order_histories 저장, order_items 일괄 저장 검증")
    void createOrder_success_flow() {
        // given
        List<CartItemRequest> items = List.of(
                new CartItemRequest(10L, "A상품", 2, 1000, 2000),
                new CartItemRequest(20L, "B상품", 1, 3000, 3000)
        );
        OrderItemRequest req = new OrderItemRequest(items);

        // when
        orderService.createOrder(req);

        // then
        // 1) Orders 저장 호출 검증 및 값 점검
        verify(ordersRepository, times(1)).save(ordersCaptor.capture());
        Orders savedOrderParam = ordersCaptor.getValue();
        assertThat(savedOrderParam.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(savedOrderParam.getTotalAmount()).isEqualTo(5000);
        assertThat(savedOrderParam.getPaidAmount()).isEqualTo(5000);
        // 주문명 규칙: "A상품 외 1개"
        assertThat(savedOrderParam.getOrderName()).isEqualTo("A상품 외 1개");

        // 2) OrderHistories 저장 호출 검증
        verify(orderHistoriesRepository, times(1)).save(historiesCaptor.capture());
        OrderHistories savedHistory = historiesCaptor.getValue();
        assertThat(savedHistory.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(savedHistory.getMessage()).isEqualTo("주문생성");
        // ordersRepository.save가 반환한 id=1L 사용됐는지
        assertThat(savedHistory.getOrderId()).isEqualTo(1L);

        // 3) OrderItems saveAll 검증
        verify(orderItemsRepository, times(1)).saveAll(orderItemsListCaptor.capture());
        List<OrderItems> savedItems = orderItemsListCaptor.getValue();
        assertThat(savedItems).hasSize(2);

        OrderItems line1 = savedItems.get(0);
        assertThat(line1.getOrderId()).isEqualTo(1L);
        assertThat(line1.getProductId()).isEqualTo(10L);
        assertThat(line1.getQuantity()).isEqualTo(2);
        assertThat(line1.getUnitPrice()).isEqualTo(1000);
        assertThat(line1.getTotalPrice()).isEqualTo(2000);

        OrderItems line2 = savedItems.get(1);
        assertThat(line2.getOrderId()).isEqualTo(1L);
        assertThat(line2.getProductId()).isEqualTo(20L);
        assertThat(line2.getQuantity()).isEqualTo(1);
        assertThat(line2.getUnitPrice()).isEqualTo(3000);
        assertThat(line2.getTotalPrice()).isEqualTo(3000);

        // 저장 순서(선택적): orders → histories → items
        InOrder inOrder = inOrder(ordersRepository, orderHistoriesRepository, orderItemsRepository);
        inOrder.verify(ordersRepository).save(any(Orders.class));
        inOrder.verify(orderHistoriesRepository).save(any(OrderHistories.class));
        inOrder.verify(orderItemsRepository).saveAll(anyList());

        // 불필요한 추가 상호작용 없음
        verifyNoMoreInteractions(ordersRepository, orderHistoriesRepository, orderItemsRepository);
    }

    @Test
    @DisplayName("주문명 생성 규칙: 0/1/N 케이스")
    void generateOrderName_cases() {
        // 0개
        assertThat(OrderService.generateOrderName(List.of())).isEqualTo("상품 없음");

        // 1개
        var one = List.of(new CartItemRequest(1L, "단일상품", 1, 1000, 1000));
        assertThat(OrderService.generateOrderName(one)).isEqualTo("단일상품");

        // N개
        var many = List.of(
                new CartItemRequest(1L, "첫상품", 1, 1000, 1000),
                new CartItemRequest(2L, "둘상품", 1, 2000, 2000),
                new CartItemRequest(3L, "셋상품", 1, 3000, 3000)
        );
        assertThat(OrderService.generateOrderName(many)).isEqualTo("첫상품 외 2개");
    }
}