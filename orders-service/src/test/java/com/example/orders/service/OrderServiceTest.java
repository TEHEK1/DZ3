package com.example.orders.service;

import com.example.orders.domain.OrderStatus;
import com.example.orders.dto.CreateOrderRequest;
import com.example.orders.outbox.OutboxMessageRepository;
import com.example.orders.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({OrderService.class, OrderServiceTest.Config.class})
class OrderServiceTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OutboxMessageRepository outboxRepository;

    @BeforeEach
    void setUp() {
        // ensure repositories empty
        orderRepository.deleteAll();
        outboxRepository.deleteAll();
    }

    @Test
    void createOrder_savesOrderAndOutbox() {
        Long userId = 1L;
        CreateOrderRequest req = new CreateOrderRequest();
        req.setAmount(new BigDecimal("100.00"));
        req.setDescription("test order");

        var response = orderService.createOrder(userId, req);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(orderRepository.findAll()).hasSize(1);
        assertThat(outboxRepository.findAll()).hasSize(1);
        var outbox = outboxRepository.findAll().get(0);
        assertThat(outbox.isSent()).isFalse();
    }

    @Test
    void updateOrderStatus_changesStatus() {
        Long userId = 2L;
        CreateOrderRequest req = new CreateOrderRequest();
        req.setAmount(new BigDecimal("50"));
        req.setDescription("desc");
        var resp = orderService.createOrder(userId, req);

        orderService.updateOrderStatus(resp.getId(), true);

        var order = orderRepository.findById(resp.getId()).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.FINISHED);
    }

    @org.springframework.boot.test.context.TestConfiguration
    static class Config {
        @org.springframework.context.annotation.Bean
        com.fasterxml.jackson.databind.ObjectMapper objectMapper() {
            return new com.fasterxml.jackson.databind.ObjectMapper();
        }
    }
} 