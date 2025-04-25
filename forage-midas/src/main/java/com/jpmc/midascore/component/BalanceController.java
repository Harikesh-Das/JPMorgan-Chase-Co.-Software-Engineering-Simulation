package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Balance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BalanceController {
    private static final Logger logger = LoggerFactory.getLogger(BalanceController.class);
    
    private final DatabaseConduit databaseConduit;
    
    public BalanceController(DatabaseConduit databaseConduit) {
        this.databaseConduit = databaseConduit;
    }
    
    @GetMapping("/balance")
    public Balance getBalance(@RequestParam Long userId) {
        logger.info("Balance requested for userId: {}", userId);
        
        UserRecord user = databaseConduit.findUserById(userId);
        
        if (user == null) {
            logger.warn("User not found for userId: {}", userId);
            return new Balance(0);
        }
        
        float balance = user.getBalance();
        logger.info("Balance for userId {}: {}", userId, balance);
        
        return new Balance(balance);
    }
} 