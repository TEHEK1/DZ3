package com.example.payments.controller;

import com.example.payments.domain.Account;
import com.example.payments.service.AccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    private Long parseUserId(String header) {
        return Long.parseLong(header);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Account createAccount(@RequestHeader("X-USER-ID") String userIdHeader) {
        return accountService.createAccount(parseUserId(userIdHeader));
    }

    @PostMapping("/topup")
    public Account topUp(@RequestHeader("X-USER-ID") String userIdHeader,
                         @Valid @RequestBody TopUpRequest request) {
        return accountService.topUp(parseUserId(userIdHeader), request.getAmount());
    }

    @GetMapping("/balance")
    public BigDecimal balance(@RequestHeader("X-USER-ID") String userIdHeader) {
        return accountService.getBalance(parseUserId(userIdHeader));
    }

    @Data
    public static class TopUpRequest {
        @DecimalMin("0.01")
        private BigDecimal amount;
    }
} 