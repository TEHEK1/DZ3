package com.example.orders.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateOrderRequest {
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotBlank
    private String description;
} 