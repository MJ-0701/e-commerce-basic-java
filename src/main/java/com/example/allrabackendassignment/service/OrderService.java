package com.example.allrabackendassignment.service;

import com.example.allrabackendassignment.domain.order.entity.*;
import com.example.allrabackendassignment.domain.order.repository.OrderHistoriesRepository;
import com.example.allrabackendassignment.domain.order.repository.OrderItemsRepository;
import com.example.allrabackendassignment.domain.order.repository.OrdersRepository;
import com.example.allrabackendassignment.web.dto.internal.order.request.OrderItemRequest;
import com.example.allrabackendassignment.web.dto.internal.order.request.OrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final PaymentService paymentService;
    private final OrdersRepository ordersRepository;
    private final OrderHistoriesRepository orderHistoriesRepository;
    private final OrderItemsRepository orderItemsRepository;

    /**
     * 유저기능은 이미 구현돼 있다고 가정 (JWT 토큰에서 유저 정보를 가져온다)
     */

    @Transactional
    public void createOrder(OrderItemRequest orderItemRequest) {
        OrderRequest orderRequest = new OrderRequest(
                GeneratedUUID.create("OID", LocalDateTime.now()),
                generateOrderName(orderItemRequest.items()),
                "김민수",
                1L,
                OrderStatus.CREATED,
                orderItemRequest.totalOrderPrice(),
                orderItemRequest.totalOrderPrice(), // 할인로직 정책이 없으므로 일단은 총합과 실 결제금액은 같다.
                0, // 초기 주문생성 시점에 환불금액은 0
                false // 전체환불여부
        );

        Orders ordersEntity = Orders
                .builder()
                .orderId(orderRequest.orderId())
                .orderName(orderRequest.orderName())
                .userName(orderRequest.userName())
                .userId(orderRequest.userId())
                .orderStatus(orderRequest.orderStatus())
                .totalAmount(orderRequest.totalAmount())
                .paidAmount(orderRequest.paidAmount())
                .canceledAmount(orderRequest.canceledAmount())
                .isFullyCanceled(orderRequest.isFullyCanceled())
                .build();

        Orders orders = ordersRepository.save(ordersEntity);

        OrderHistories orderHistoriesEntity = OrderHistories
                .builder()
                .orderId(orders.getId())
                .changedBy("SYSTEM")
                .message("주문생성")
                .status(OrderStatus.CREATED)
                .build();
        orderHistoriesRepository.save(orderHistoriesEntity);

        List<OrderItems> orderItems = orderItemRequest.items().stream()
                .map(i -> OrderItems.builder()
                        .orderId(orders.getId())
                        .productId(i.productId())
                        .quantity(i.quantity())
                        .unitPrice(i.unitPrice())
                        .totalPrice(i.totalPrice())
                        .build())
                .toList();

        orderItemsRepository.saveAll(orderItems);

    }

    public static String generateOrderName(List<OrderItemRequest.CartItemRequest> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            return "상품 없음";
        }
        if (orderItems.size() == 1) {
            return orderItems.get(0).productName();
        }
        return orderItems.get(0).productName() + " 외 " + (orderItems.size() - 1) + "개";
    }

}
