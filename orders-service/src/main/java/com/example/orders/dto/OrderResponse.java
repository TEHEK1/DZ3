package com.example.orders.dto;

import com.example.orders.domain.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private BigDecimal amount;
    private String description;
    private OrderStatus status;
    private Instant createdAt;
} 