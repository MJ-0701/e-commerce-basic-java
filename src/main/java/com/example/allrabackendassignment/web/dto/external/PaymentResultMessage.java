package com.example.allrabackendassignment.web.dto.external;

import java.io.Serializable;

public record PaymentResultMessage(
        String correlationId,
        Long orderPk,
        String orderId,
        boolean success,
        String failureReason,
        PaymentResponse response
) implements Serializable {

    public static PaymentResultMessage success(String correlationId, Long orderPk, String orderId, PaymentResponse resp) {
        return new PaymentResultMessage(correlationId, orderPk, orderId, true, null, resp);
    }

    public static PaymentResultMessage failure(String correlationId, Long orderPk, String orderId, String reason) {
        return new PaymentResultMessage(correlationId, orderPk, orderId, false, reason, null);
    }
}