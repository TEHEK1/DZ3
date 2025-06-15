package com.example.payments.service;

import com.example.payments.domain.Account;
import com.example.payments.repository.AccountRepository;
import jakarta.persistence.LockModeType;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final EntityManager entityManager;

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
        Account account = lockAccount(userId);
        account.setBalance(account.getBalance().add(amount));
        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance(Long userId) {
        return accountRepository.findById(userId).map(Account::getBalance).orElse(BigDecimal.ZERO);
    }

    @Transactional
    public boolean withdraw(Long userId, BigDecimal amount) {
        Account account = lockAccount(userId);
        if (account.getBalance().compareTo(amount) < 0) {
            return false;
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
        return true;
    }

    private Account lockAccount(Long userId) {
        Account account = entityManager.find(Account.class, userId, LockModeType.PESSIMISTIC_WRITE);
        if (account == null) {
            throw new RuntimeException("Account not found for user " + userId);
        }
        return account;
    }
} 