package com.example.orders.messaging;

import com.example.orders.events.PaymentResultEvent;
import com.example.orders.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentResultListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentResultListener.class);

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "payment-results")
    public void handlePaymentResult(String message) {
        try {
            PaymentResultEvent event = objectMapper.readValue(message, PaymentResultEvent.class);
            orderService.updateOrderStatus(event.getOrderId(), event.isSuccess());
            log.info("Processed payment result for order {}", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to process payment result", e);
        }
    }
} 