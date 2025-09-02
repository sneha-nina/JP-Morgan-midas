package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransactionListener {

    private static final Logger log = LoggerFactory.getLogger(TransactionListener.class);

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public TransactionListener(UserRepository userRepository,
                               TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    @KafkaListener(topics = "${general.kafka-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<String, Transaction> record) {
        Transaction transaction = record.value();
        log.info("✅ Received transaction: {}", transaction);

        long senderId = transaction.getSenderId();
        long recipientId = transaction.getRecipientId();
        float amount = transaction.getAmount();

        // 1️⃣ Fetch sender and recipient (fail early if not found)
        UserRecord sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalStateException("Sender not found: " + senderId));
        UserRecord recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new IllegalStateException("Recipient not found: " + recipientId));

        // 2️⃣ Validate sender balance
        if (sender.getBalance() < amount) {
            log.warn("❌ Transaction skipped: insufficient funds for sender {} (balance={}, required={})",
                    senderId, sender.getBalance(), amount);
            return;
        }

        // 3️⃣ Update balances
        sender.setBalance(sender.getBalance() - amount);
        recipient.setBalance(recipient.getBalance() + amount);
        userRepository.save(sender);
        userRepository.save(recipient);
        // <-- BREAKPOINT HERE: inspect sender.getBalance(), recipient.getBalance()

        // 4️⃣ Persist transaction record
        TransactionRecord txRecord = new TransactionRecord();
        txRecord.setSender(sender);
        txRecord.setRecipient(recipient);
        txRecord.setAmount(amount);
        transactionRepository.save(txRecord);

        log.info("💾 Transaction recorded successfully: {} -> {} | amount {}", senderId, recipientId, amount);
    }

    // 5️⃣ Conditional log for Waldorf (debugging helper)
}
