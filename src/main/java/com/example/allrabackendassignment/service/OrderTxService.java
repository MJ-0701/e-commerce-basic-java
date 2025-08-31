package com.example.allrabackendassignment.service;

import com.example.allrabackendassignment.domain.order.entity.*;
import com.example.allrabackendassignment.domain.order.repository.OrderHistoriesRepository;
import com.example.allrabackendassignment.domain.order.repository.OrderItemsRepository;
import com.example.allrabackendassignment.domain.order.repository.OrdersRepository;
import com.example.allrabackendassignment.domain.product.entity.Product;
import com.example.allrabackendassignment.domain.product.repository.ProductRepository;
import com.example.allrabackendassignment.global.http.exception.InsufficientStockException;
import com.example.allrabackendassignment.web.dto.internal.order.request.OrderItemRequest;
import com.example.allrabackendassignment.web.dto.internal.order.request.OrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 트랜잭션 전용 서비스:
 * - 주문/아이템/재고: REQUIRED (한 트랜잭션)
 * - 상태 변경: REQUIRES_NEW (결제 결과와 독립적으로 커밋)
 */
@Service
@RequiredArgsConstructor
public class OrderTxService {

    private final OrdersRepository ordersRepository;
    private final OrderHistoriesRepository orderHistoriesRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final ProductRepository productRepository;

    @Transactional(propagation = Propagation.REQUIRED)
    public Orders createOrderRecords(OrderItemRequest orderItemRequest) {
        Map<Long, Integer> requestedQtyByProductId = orderItemRequest.items().stream()
                .collect(Collectors.toMap(
                        OrderItemRequest.CartItemRequest::productId,
                        OrderItemRequest.CartItemRequest::quantity,
                        Integer::sum
                ));

        // 비관적 락 + 재고 차감
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
                throw new InsufficientStockException(productId, needQty, available);
            }
            product.decreaseStock(needQty);
        }

        OrderRequest orderRequest = new OrderRequest(
                GeneratedUUID.create("OID", LocalDateTime.now()),
                OrderService.generateOrderName(orderItemRequest.items()),
                "김민수",
                1L,
                OrderStatus.CREATED,
                orderItemRequest.totalOrderPrice(),
                orderItemRequest.totalOrderPrice(),
                0,
                false
        );

        Orders ordersEntity = Orders.builder()
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

        OrderHistories createdHistory = OrderHistories.builder()
                .orderId(orders.getId())
                .changedBy("SYSTEM")
                .message("주문생성")
                .status(OrderStatus.CREATED)
                .build();
        orderHistoriesRepository.save(createdHistory);

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

        return orders;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markOrderPaid(Long orderPk) {
        Orders o = ordersRepository.findById(orderPk)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderPk));
        o.updateOrderStatus(OrderStatus.COMPLETED);
        ordersRepository.save(o);

        OrderHistories h = OrderHistories.builder()
                .orderId(orderPk)
                .changedBy("SYSTEM")
                .message("결제성공")
                .status(OrderStatus.COMPLETED)
                .build();
        orderHistoriesRepository.save(h);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markOrderPaymentFailed(Long orderPk, String reason) {
        Orders o = ordersRepository.findById(orderPk)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderPk));
        o.updateOrderStatus(OrderStatus.FAILED);
        ordersRepository.save(o);

        OrderHistories h = OrderHistories.builder()
                .orderId(orderPk)
                .changedBy("SYSTEM")
                .message("결제실패: " + reason)
                .status(OrderStatus.FAILED)
                .build();
        orderHistoriesRepository.save(h);
    }

    // 주문 생성 시 저장했던 아이템 수량을 이용해 재고 원복 + 주문 취소 처리
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void compensateOrderCreation(Long orderId) {
        Orders o = ordersRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        // 1) 재고 원복
        List<OrderItems> items = orderItemsRepository.findByOrderId(orderId);
        for (OrderItems it : items) {
            Product p = productRepository.findByIdForUpdate(it.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + it.getProductId()));
            p.increaseStock(it.getQuantity()); // decreaseStock 반대 동작
        }

        // 2) 주문 상태 취소(or 삭제)
        o.updateOrderStatus(OrderStatus.CANCELED); // 또는 soft delete 정책에 맞게
        ordersRepository.save(o);

        // 3) 히스토리 남기기 (길이 방어)
        String msg = "결제실패로 주문 취소(보상 트랜잭션)";
        OrderHistories h = OrderHistories.builder()
                .orderId(orderId)
                .changedBy("SYSTEM")
                .message(msg)
                .status(OrderStatus.CANCELED)
                .build();
        orderHistoriesRepository.save(h);
    }
}
