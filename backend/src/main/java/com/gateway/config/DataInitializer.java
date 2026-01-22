package com.gateway.config;

import com.gateway.models.Merchant;
import com.gateway.repositories.MerchantRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final MerchantRepository merchantRepository;

    @Value("${gateway.test.merchant.email}")
    private String testEmail;

    @Value("${gateway.test.merchant.key}")
    private String testKey;

    @Value("${gateway.test.merchant.secret}")
    private String testSecret;

    public DataInitializer(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Check if the test merchant already exists
        Optional<Merchant> existingMerchant = merchantRepository.findByApiKey(testKey);

        if (existingMerchant.isEmpty()) {
            System.out.println("Initializing Test Merchant...");
            
            Merchant merchant = new Merchant();
            merchant.setEmail(testEmail);
            merchant.setApiKey(testKey);
            merchant.setApiSecret(testSecret);
            merchant.setWebhookSecret("whsec_test_abc123");

            merchantRepository.save(merchant);
            System.out.println("Test Merchant Created Successfully: " + testEmail);
        } else {
            System.out.println("Test Merchant already exists.");
            // Update webhook secret if not set
            Merchant merchant = existingMerchant.get();
            if (merchant.getWebhookSecret() == null || merchant.getWebhookSecret().isEmpty()) {
                merchant.setWebhookSecret("whsec_test_abc123");
                merchantRepository.save(merchant);
            }
        }
    }
}