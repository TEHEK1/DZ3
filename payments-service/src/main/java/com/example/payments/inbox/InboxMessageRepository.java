package com.example.payments.inbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InboxMessageRepository extends JpaRepository<InboxMessage, UUID> {
    Optional<InboxMessage> findByMessageId(String messageId);
} 