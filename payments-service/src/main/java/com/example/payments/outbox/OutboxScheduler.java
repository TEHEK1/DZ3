package com.example.payments.outbox;

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

    private final OutboxMessageRepository repo;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void sendMessages() {
        repo.findBySentFalse().forEach(msg -> {
            kafkaTemplate.send("payment-results", msg.getPayload());
            msg.setSent(true);
            repo.save(msg);
            log.info("Sent payment result message {}", msg.getId());
        });
    }
} 