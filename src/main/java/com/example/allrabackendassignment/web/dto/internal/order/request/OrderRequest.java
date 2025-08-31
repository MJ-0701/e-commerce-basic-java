package com.example.allrabackendassignment.web.dto.internal.order.request;

import com.example.allrabackendassignment.domain.order.entity.GeneratedUUID;
import com.example.allrabackendassignment.domain.order.entity.OrderStatus;

public record OrderRequest(
        GeneratedUUID orderId, String orderName, String userName, Long userId,
        OrderStatus orderStatus, int totalAmount, int paidAmount, int canceledAmount, Boolean isFullyCanceled
) {
}
