package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Balance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    private final DatabaseConduit databaseConduit;
    
    public UserController(DatabaseConduit databaseConduit) {
        this.databaseConduit = databaseConduit;
    }
    
    @GetMapping("/balance")
    public ResponseEntity<Balance> getBalance(@RequestParam Long userId) {
        UserRecord user = databaseConduit.findUserById(userId);
        if (user == null) {
            logger.warn("User not found: {}", userId);
            return ResponseEntity.notFound().build();
        }
        
        Balance balance = new Balance(user.getBalance());
        return ResponseEntity.ok(balance);
    }
    
    @GetMapping("/user")
    public ResponseEntity<UserRecord> getUserByName(@RequestParam String name) {
        UserRecord user = databaseConduit.findUserByName(name);
        if (user == null) {
            logger.warn("User not found by name: {}", name);
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(user);
    }
} 