package com.example.payments.service;

import com.example.payments.domain.Account;
import com.example.payments.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public Account createAccount(Long userId) {
        if (accountRepository.existsById(userId)) {
            throw new RuntimeException("Account already exists for user " + userId);
        }
        Account account = Account.builder().userId(userId).balance(BigDecimal.ZERO).build();
        return accountRepository.save(account);
    }

    @Transactional
    public Account topUp(Long userId, BigDecimal amount) {
        Account account = accountRepository.findById(userId).orElseThrow(() -> new RuntimeException("Account not found for user " + userId));
        account.setBalance(account.getBalance().add(amount));
        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance(Long userId) {
        return accountRepository.findById(userId).map(Account::getBalance).orElse(BigDecimal.ZERO);
    }

    @Transactional
    public boolean withdraw(Long userId, BigDecimal amount) {
        Account account = accountRepository.findById(userId).orElseThrow(() -> new RuntimeException("Account not found for user " + userId));
        if (account.getBalance().compareTo(amount) < 0) {
            return false;
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
        return true;
    }
} 