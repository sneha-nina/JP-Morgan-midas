package com.jpmc.midascore.service;

import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.model.Incentive;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    private static final String INCENTIVE_API_URL = "http://localhost:8080/incentive";

    public TransactionService(TransactionRepository transactionRepository,
                              UserRepository userRepository,
                              RestTemplate restTemplate) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public Transaction processTransaction(Transaction transaction) {
        // 1. Call Incentive API
        Incentive incentive = restTemplate.postForObject(
                INCENTIVE_API_URL,
                transaction,
                Incentive.class
        );

        float incentiveAmount = (incentive != null) ? incentive.getAmount() : 0.0f;
        transaction.setIncentive(incentiveAmount);

        // 2. Load sender & recipient
        UserRecord sender = userRepository.findById(transaction.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        UserRecord recipient = userRepository.findById(transaction.getRecipientId())
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        // 3. Validate balance
        if (sender.getBalance() < transaction.getAmount()) {
            throw new IllegalStateException("Insufficient balance for sender");
        }

        // 4. Update balances
        sender.setBalance(sender.getBalance() - transaction.getAmount());
        recipient.setBalance(recipient.getBalance() + transaction.getAmount() + incentiveAmount);

        userRepository.save(sender);
        userRepository.save(recipient);

        // 5. Persist transaction record
        TransactionRecord record = toRecord(transaction, sender, recipient);
        transactionRepository.save(record);

        return transaction;
    }

    private TransactionRecord toRecord(Transaction t, UserRecord sender, UserRecord recipient) {
        TransactionRecord record = new TransactionRecord();
        record.setSender(sender);
        record.setRecipient(recipient);
        record.setAmount(t.getAmount());
        record.setIncentive(t.getIncentive());
        record.setTimestamp(LocalDateTime.now());
        return record;
    }
}
