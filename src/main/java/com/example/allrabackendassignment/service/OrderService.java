package com.example.allrabackendassignment.service;

import com.example.allrabackendassignment.domain.order.entity.*;
import com.example.allrabackendassignment.domain.order.repository.OrderHistoriesRepository;
import com.example.allrabackendassignment.domain.order.repository.OrderItemsRepository;
import com.example.allrabackendassignment.domain.order.repository.OrdersRepository;
import com.example.allrabackendassignment.domain.product.entity.Product;
import com.example.allrabackendassignment.domain.product.repository.ProductRepository;
import com.example.allrabackendassignment.event.OrderCreatedEvent;
import com.example.allrabackendassignment.global.http.exception.InsufficientStockException;
import com.example.allrabackendassignment.web.dto.external.PaymentRequest;
import com.example.allrabackendassignment.web.dto.internal.order.request.OrderItemRequest;
import com.example.allrabackendassignment.web.dto.internal.order.request.OrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrdersRepository ordersRepository;
    private final OrderHistoriesRepository orderHistoriesRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 유저기능은 이미 구현돼 있다고 가정 (JWT 토큰에서 유저 정보를 가져온다)
     */

    @Transactional
    public void createOrder(OrderItemRequest orderItemRequest) {

        // 0) 같은 상품이 리스트에 중복으로 들어올 수 있으니 productId 기준으로 수량 합산(안전장치)
        Map<Long, Integer> requestedQtyByProductId = orderItemRequest.items().stream()
                .collect(Collectors.toMap(
                        OrderItemRequest.CartItemRequest::productId,
                        OrderItemRequest.CartItemRequest::quantity,
                        Integer::sum
                ));

        // 1) 재고 검증 & 차감 (비관적 락으로 동시성 방어)
        Map<Long, Product> lockedProducts = new HashMap<>();
        for (Map.Entry<Long, Integer> e : requestedQtyByProductId.entrySet()) {
            Long productId = e.getKey();
            int needQty = e.getValue();

            Product product = productRepository.findByIdForUpdate(productId)
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다. productId=" + productId));

            if (Boolean.FALSE.equals(product.getIsActive())) {
                throw new IllegalStateException("비활성 상품입니다. productId=" + productId);
            }
            int available = product.getStock();
            if (available < needQty) {
                throw new InsufficientStockException(productId, needQty, available); // RuntimeException → 전체 롤백
            }

            product.decreaseStock(needQty); // @DynamicUpdate로 필요한 컬럼만 업데이트
            lockedProducts.put(productId, product);
        }

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

        // 결제요청은 같은 트랜잭션에서 호출하지 않고, 커밋 이후에 처리하도록 이벤트 발행
        eventPublisher.publishEvent(new OrderCreatedEvent(
                orders.getOrderId().toString(),
                orders.getPaidAmount()
        ));



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
