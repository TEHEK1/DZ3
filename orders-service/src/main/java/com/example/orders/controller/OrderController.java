package com.example.orders.controller;

import com.example.orders.dto.CreateOrderRequest;
import com.example.orders.dto.OrderResponse;
import com.example.orders.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // For demonstration we get userId from header; in real scenario we have auth context.
    private Long getUserIdFromHeader(String userIdHeader) {
        return Long.parseLong(userIdHeader);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@RequestHeader("X-USER-ID") String userIdHeader,
                                     @Valid @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(getUserIdFromHeader(userIdHeader), request);
    }

    @GetMapping
    public List<OrderResponse> listOrders(@RequestHeader("X-USER-ID") String userIdHeader) {
        return orderService.getOrders(getUserIdFromHeader(userIdHeader));
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@RequestHeader("X-USER-ID") String userIdHeader,
                                  @PathVariable Long orderId) {
        return orderService.getOrder(orderId, getUserIdFromHeader(userIdHeader));
    }
} 