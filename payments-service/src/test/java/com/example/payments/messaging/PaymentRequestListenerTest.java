package com.example.payments.messaging;

import com.example.payments.events.PaymentRequestedEvent;
import com.example.payments.outbox.OutboxMessageRepository;
import com.example.payments.inbox.InboxMessageRepository;
import com.example.payments.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PaymentRequestListenerTest {

    @Autowired
    PaymentRequestListener listener;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    AccountService accountService;
    @Autowired
    OutboxMessageRepository outboxRepo;

    @MockBean
    InboxMessageRepository inboxRepo; // we will not focus on idempotency here

    @Test
    void processesPaymentAndCreatesOutbox() throws Exception {
        Long user = 55L;
        accountService.createAccount(user);
        accountService.topUp(user, new BigDecimal("100"));
        PaymentRequestedEvent event = new PaymentRequestedEvent(999L, user, new BigDecimal("30"));
        String payload = mapper.writeValueAsString(event);
        Message msg = MessageBuilder.withBody(payload.getBytes(StandardCharsets.UTF_8))
                .setMessageId(UUID.randomUUID().toString())
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .build();

        listener.onPaymentRequest(payload, msg);

        assertThat(outboxRepo.findAll()).hasSize(1);
        assertThat(accountService.getBalance(user)).isEqualByComparingTo("70");
    }
} 