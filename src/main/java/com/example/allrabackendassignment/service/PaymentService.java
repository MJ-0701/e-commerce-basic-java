package com.example.allrabackendassignment.service;

import com.example.allrabackendassignment.external.PaymentFeignClient;
import com.example.allrabackendassignment.web.dto.external.PaymentRequest;
import com.example.allrabackendassignment.web.dto.external.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentFeignClient paymentFeignClient;

    public PaymentResponse requestPayment(@RequestBody PaymentRequest request){

        return paymentFeignClient.requestPayment(request);
    }
}
