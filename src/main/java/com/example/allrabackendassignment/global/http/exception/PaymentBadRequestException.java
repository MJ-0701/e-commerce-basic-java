package com.example.allrabackendassignment.global.http.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Getter
public class PaymentBadRequestException extends RuntimeException {
    private final String rawBody;

    public PaymentBadRequestException(String rawBody) {
        super("PG returned 400");
        this.rawBody = rawBody;
    }

}
