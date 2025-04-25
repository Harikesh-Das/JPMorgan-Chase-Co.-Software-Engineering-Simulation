package com.jpmc.midascore.component;

import com.jpmc.midascore.foundation.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class KafkaConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);
    private final List<Transaction> receivedTransactions = new ArrayList<>();
    private final DatabaseConduit databaseConduit;
    
    public KafkaConsumer(DatabaseConduit databaseConduit) {
        this.databaseConduit = databaseConduit;
    }
    
    @KafkaListener(topics = "${general.kafka-topic}")
    public void listen(Transaction transaction) {
        logger.info("Received transaction: {}", transaction);
        receivedTransactions.add(transaction);
        
        // Process the transaction
        boolean success = databaseConduit.processTransaction(transaction);
        
        if (success) {
            logger.info("Transaction processed successfully: {}", transaction);
        } else {
            logger.warn("Failed to process transaction: {}", transaction);
        }
    }
    
    public List<Transaction> getReceivedTransactions() {
        return receivedTransactions;
    }
} 