package com.example.orders.service;

import com.example.orders.domain.Order;
import com.example.orders.domain.OrderStatus;
import com.example.orders.dto.CreateOrderRequest;
import com.example.orders.dto.OrderResponse;
import com.example.orders.events.PaymentRequestedEvent;
import com.example.orders.outbox.OutboxMessage;
import com.example.orders.outbox.OutboxMessageRepository;
import com.example.orders.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxMessageRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        Order order = Order.builder()
                .userId(userId)
                .amount(request.getAmount())
                .description(request.getDescription())
                .status(OrderStatus.NEW)
                .createdAt(Instant.now())
                .build();

        order = orderRepository.save(order);

        PaymentRequestedEvent event = new PaymentRequestedEvent(order.getId(), userId, order.getAmount());
        try {
            String payload = objectMapper.writeValueAsString(event);
            OutboxMessage outboxMessage = OutboxMessage.builder()
                    .id(UUID.randomUUID())
                    .aggregateType("Order")
                    .aggregateId(String.valueOf(order.getId()))
                    .payload(payload)
                    .createdAt(Instant.now())
                    .sent(false)
                    .build();
            outboxRepository.save(outboxMessage);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event", e);
        }

        return toResponse(order);
    }

    public List<OrderResponse> getOrders(Long userId) {
        return orderRepository.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    public OrderResponse getOrder(Long orderId, Long userId) {
        return orderRepository.findById(orderId)
                .filter(o -> o.getUserId().equals(userId))
                .map(this::toResponse)
                .orElseThrow();
    }

    @Transactional
    public void updateOrderStatus(Long orderId, boolean success) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus(success ? OrderStatus.FINISHED : OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    private OrderResponse toResponse(Order o) {
        return OrderResponse.builder()
                .id(o.getId())
                .amount(o.getAmount())
                .description(o.getDescription())
                .status(o.getStatus())
                .createdAt(o.getCreatedAt())
                .build();
    }
} 