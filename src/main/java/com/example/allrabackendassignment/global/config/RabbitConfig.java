package com.example.allrabackendassignment.global.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String PAYMENT_REQUEST_QUEUE = "payment.request.queue";
    public static final String PAYMENT_RESULT_QUEUE = "payment.result.queue";
    public static final String PAYMENT_RETRY_EXCHANGE = "payment.retry.exchange";
    public static final String PAYMENT_RETRY_QUEUE = "payment.retry.queue";

    public static final String RK_PAYMENT_REQUEST = "payment.request";
    public static final String RK_PAYMENT_RESULT = "payment.result";

    @Bean DirectExchange paymentExchange() { return new DirectExchange(PAYMENT_EXCHANGE, true, false); }
    @Bean DirectExchange paymentRetryExchange() { return new DirectExchange(PAYMENT_RETRY_EXCHANGE, true, false); }

    @Bean Queue paymentRequestQueue() {
        return QueueBuilder.durable(PAYMENT_REQUEST_QUEUE)
                .withArgument("x-dead-letter-exchange", PAYMENT_RETRY_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RK_PAYMENT_REQUEST)
                .build();
    }

    @Bean Binding paymentRequestBinding() {
        return BindingBuilder.bind(paymentRequestQueue()).to(paymentExchange()).with(RK_PAYMENT_REQUEST);
    }

    @Bean Queue paymentRetryQueue() {
        return QueueBuilder.durable(PAYMENT_RETRY_QUEUE)
                .withArgument("x-message-ttl", 10_000)
                .withArgument("x-dead-letter-exchange", PAYMENT_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RK_PAYMENT_REQUEST)
                .build();
    }

    @Bean Binding paymentRetryBinding() {
        return BindingBuilder.bind(paymentRetryQueue()).to(paymentRetryExchange()).with(RK_PAYMENT_REQUEST);
    }

    @Bean Queue paymentResultQueue() { return QueueBuilder.durable(PAYMENT_RESULT_QUEUE).build(); }
    @Bean Binding paymentResultBinding() {
        return BindingBuilder.bind(paymentResultQueue()).to(paymentExchange()).with(RK_PAYMENT_RESULT);
    }

    // ★ Jackson 컨버터 명시
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf, Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(cf);
        template.setMessageConverter(converter);
        return template;
    }
}