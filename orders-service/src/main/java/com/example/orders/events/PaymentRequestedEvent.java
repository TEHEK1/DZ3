package com.example.orders.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestedEvent {
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
} 