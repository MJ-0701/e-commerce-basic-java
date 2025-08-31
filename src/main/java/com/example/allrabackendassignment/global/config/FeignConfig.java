package com.example.allrabackendassignment.global.config;

import com.example.allrabackendassignment.global.http.ErrorCode;
import com.example.allrabackendassignment.global.http.exception.BusinessException;
import com.example.allrabackendassignment.global.http.exception.PaymentBadRequestException;
import feign.Util;
import feign.codec.Decoder;
import feign.codec.ErrorDecoder;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableFeignClients(basePackages = "com.example.allrabackendassignment")
public class FeignConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            String body = "";
            try {
                if (response.body() != null) {
                    body = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
                }
            } catch (Exception ignore) {}

            if (response.status() == 400) {
                return new BusinessException(ErrorCode.PAYMENT_FAILED);
            }
            if (response.status() == 504) {
                return new BusinessException(ErrorCode.PAYMENT_TIMEOUT);
            }

            return new BusinessException(ErrorCode.PAYMENT_GATEWAY_ERROR);
        };
    }
}
