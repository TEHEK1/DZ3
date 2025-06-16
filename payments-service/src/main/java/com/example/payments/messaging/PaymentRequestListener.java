package com.example.payments.messaging;

import com.example.payments.events.PaymentRequestedEvent;
import com.example.payments.events.PaymentResultEvent;
import com.example.payments.inbox.InboxMessage;
import com.example.payments.inbox.InboxMessageRepository;
import com.example.payments.outbox.OutboxMessage;
import com.example.payments.outbox.OutboxMessageRepository;
import com.example.payments.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentRequestListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentRequestListener.class);

    private final ObjectMapper objectMapper;
    private final AccountService accountService;
    private final InboxMessageRepository inboxRepository;
    private final OutboxMessageRepository outboxRepository;

    @KafkaListener(topics = "payment-requests", groupId = "payments-service")
    @Transactional
    public void onPaymentRequest(String message, org.apache.kafka.clients.consumer.ConsumerRecord<String, String> record) {
        try {
            String messageId = record.headers().lastHeader("messageId") != null ? new String(record.headers().lastHeader("messageId").value()) : null;
            if (messageId == null) {
                messageId = UUID.randomUUID().toString();
            }
            // Idempotency check
            if (inboxRepository.findByMessageId(messageId).isPresent()) {
                log.info("Duplicate message {} ignored", messageId);
                return;
            }
            inboxRepository.save(InboxMessage.builder()
                    .messageId(messageId)
                    .receivedAt(Instant.now())
                    .build());

            PaymentRequestedEvent event = objectMapper.readValue(message, PaymentRequestedEvent.class);

            boolean success = accountService.withdraw(event.getUserId(), event.getAmount());
            PaymentResultEvent resultEvent = new PaymentResultEvent(event.getOrderId(), success);
            String payload = objectMapper.writeValueAsString(resultEvent);
            OutboxMessage outbox = OutboxMessage.builder()
                    .payload(payload)
                    .createdAt(Instant.now())
                    .sent(false)
                    .build();
            outboxRepository.save(outbox);
            log.info("Processed payment for order {} success {}", event.getOrderId(), success);
        } catch (Exception e) {
            log.error("Failed to process payment request", e);
            throw new RuntimeException(e);
        }
    }
} 