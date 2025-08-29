package com.example.allrabackendassignment.external;

import com.example.allrabackendassignment.global.config.FeignConfig;
import com.example.allrabackendassignment.web.dto.external.PaymentRequest;
import com.example.allrabackendassignment.web.dto.external.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "paymentClient",
        configuration = FeignConfig.class,
        url = "https://allra-payment.free.beeceptor.com"
)
public interface PaymentFeignClient {

    @PostMapping(value = "/api/v1/payment", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    PaymentResponse requestPayment(@RequestBody PaymentRequest request);
}
