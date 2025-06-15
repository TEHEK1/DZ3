package com.example.payments.service;

import com.example.payments.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(AccountService.class)
class AccountServiceTest {

    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void clean() {
        accountRepository.deleteAll();
    }

    @Test
    void createAccount_initialBalanceZero() {
        var acc = accountService.createAccount(1L);
        assertThat(acc.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void duplicateAccountThrows() {
        accountService.createAccount(2L);
        assertThatThrownBy(() -> accountService.createAccount(2L)).isInstanceOf(RuntimeException.class);
    }

    @Test
    void topUpAddsBalance() {
        accountService.createAccount(3L);
        accountService.topUp(3L, new BigDecimal("100"));
        assertThat(accountService.getBalance(3L)).isEqualByComparingTo("100");
    }

    @Test
    void withdrawSuccessAndFail() {
        accountService.createAccount(4L);
        accountService.topUp(4L, new BigDecimal("50"));
        boolean ok = accountService.withdraw(4L, new BigDecimal("20"));
        assertThat(ok).isTrue();
        assertThat(accountService.getBalance(4L)).isEqualByComparingTo("30");

        boolean fail = accountService.withdraw(4L, new BigDecimal("40"));
        assertThat(fail).isFalse();
        assertThat(accountService.getBalance(4L)).isEqualByComparingTo("30");
    }
} 