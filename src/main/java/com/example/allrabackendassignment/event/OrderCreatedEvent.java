package com.example.allrabackendassignment.event;

public record OrderCreatedEvent(String orderPublicId, int paidAmount) {
}
