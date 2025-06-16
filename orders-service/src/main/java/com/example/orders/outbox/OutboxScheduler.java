package com.example.orders.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OutboxScheduler {
    private static final Logger log = LoggerFactory.getLogger(OutboxScheduler.class);

    private final OutboxMessageRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publishEvents() {
        outboxRepository.findBySentFalse().forEach(msg -> {
            kafkaTemplate.send("payment-requests", msg.getPayload());
            msg.setSent(true);
            outboxRepository.save(msg);
            log.info("Published outbox msg {}", msg.getId());
        });
    }
} 