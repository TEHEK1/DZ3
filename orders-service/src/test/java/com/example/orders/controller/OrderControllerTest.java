package com.example.orders.controller;

import com.example.orders.dto.CreateOrderRequest;
import com.example.orders.dto.OrderResponse;
import com.example.orders.domain.OrderStatus;
import com.example.orders.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    OrderService orderService;

    @Test
    void createOrderEndpoint() throws Exception {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setAmount(new BigDecimal("25.5"));
        req.setDescription("pizza");

        OrderResponse stub = OrderResponse.builder()
                .id(1L)
                .amount(req.getAmount())
                .description(req.getDescription())
                .status(OrderStatus.NEW)
                .build();
        org.mockito.Mockito.when(orderService.createOrder(10L, req)).thenReturn(stub);

        mockMvc.perform(post("/orders")
                        .header("X-USER-ID", "10")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("NEW"));
    }
} 