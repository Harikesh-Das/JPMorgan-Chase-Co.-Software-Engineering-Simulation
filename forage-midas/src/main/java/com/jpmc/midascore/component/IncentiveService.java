package com.jpmc.midascore.component;

import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class IncentiveService {
    private static final Logger logger = LoggerFactory.getLogger(IncentiveService.class);
    
    private final RestTemplate restTemplate;
    private final String incentiveApiUrl;
    
    public IncentiveService(RestTemplateBuilder restTemplateBuilder, 
                            @Value("${incentive.api.url:http://localhost:8080/incentive}") String incentiveApiUrl) {
        this.restTemplate = restTemplateBuilder.build();
        this.incentiveApiUrl = incentiveApiUrl;
        logger.info("Incentive API URL: {}", incentiveApiUrl);
    }
    
    public Incentive getIncentive(Transaction transaction) {
        try {
            logger.info("Requesting incentive for transaction: {}", transaction);
            Incentive incentive = restTemplate.postForObject(incentiveApiUrl, transaction, Incentive.class);
            logger.info("Received incentive: {}", incentive);
            return incentive;
        } catch (RestClientException e) {
            logger.error("Error while requesting incentive: {}", e.getMessage());
            // Return a default zero incentive in case of errors
            return new Incentive(0);
        }
    }
} 