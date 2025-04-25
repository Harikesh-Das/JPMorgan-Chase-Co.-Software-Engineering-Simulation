package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DatabaseConduit {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConduit.class);
    
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final IncentiveService incentiveService;

    public DatabaseConduit(UserRepository userRepository, 
                          TransactionRepository transactionRepository,
                          IncentiveService incentiveService) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.incentiveService = incentiveService;
    }

    public void save(UserRecord userRecord) {
        userRepository.save(userRecord);
    }
    
    public UserRecord findUserById(long userId) {
        return userRepository.findById(userId);
    }
    
    public UserRecord findUserByName(String name) {
        return userRepository.findByName(name);
    }
    
    @Transactional
    public boolean processTransaction(Transaction transaction) {
        // Find the sender and recipient
        UserRecord sender = userRepository.findById(transaction.getSenderId());
        UserRecord recipient = userRepository.findById(transaction.getRecipientId());
        
        // Validate the transaction
        if (sender == null || recipient == null) {
            logger.warn("Invalid transaction: Sender or recipient not found. Transaction: {}", transaction);
            return false;
        }
        
        if (sender.getBalance() < transaction.getAmount()) {
            logger.warn("Invalid transaction: Insufficient funds. Sender balance: {}, Amount: {}", 
                    sender.getBalance(), transaction.getAmount());
            
            // Record the failed transaction with zero incentive
            TransactionRecord transactionRecord = new TransactionRecord(
                    sender, recipient, transaction.getAmount(), 0, false);
            transactionRepository.save(transactionRecord);
            
            return false;
        }
        
        // Get incentive for the transaction
        Incentive incentive = incentiveService.getIncentive(transaction);
        float incentiveAmount = incentive != null ? incentive.getAmount() : 0;
        
        // Process the transaction
        float newSenderBalance = sender.getBalance() - transaction.getAmount();
        
        // Add both transaction amount and incentive to recipient's balance
        float newRecipientBalance = recipient.getBalance() + transaction.getAmount() + incentiveAmount;
        
        sender.setBalance(newSenderBalance);
        recipient.setBalance(newRecipientBalance);
        
        userRepository.save(sender);
        userRepository.save(recipient);
        
        // Record the transaction with incentive
        TransactionRecord transactionRecord = new TransactionRecord(
                sender, recipient, transaction.getAmount(), incentiveAmount, true);
        transactionRepository.save(transactionRecord);
        
        logger.info("Transaction processed successfully: {}", transaction);
        logger.info("Incentive amount: {}", incentiveAmount);
        logger.info("New sender balance: {}, New recipient balance: {}", 
                newSenderBalance, newRecipientBalance);
        
        return true;
    }
}
