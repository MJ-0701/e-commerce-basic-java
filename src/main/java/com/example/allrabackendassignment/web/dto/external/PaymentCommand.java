package com.example.allrabackendassignment.web.dto.external;

import java.io.Serializable;

public record PaymentCommand(
        String correlationId,
        Long orderPk,
        String orderId,
        int attempt,
        int maxAttempts,
        int amount,
        PaymentRequest request // 기존 PaymentRequest 재사용
) implements Serializable {}