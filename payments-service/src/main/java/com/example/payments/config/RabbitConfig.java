package com.example.payments.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Bean
    public Queue paymentRequestsQueue() {
        return new Queue("payment-requests", true);
    }

    @Bean
    public Queue paymentResultsQueue() {
        return new Queue("payment-results", true);
    }
} 