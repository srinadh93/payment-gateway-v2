package com.gateway.repositories;

import com.gateway.models.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MerchantRepository extends JpaRepository<Merchant, String> {
    Optional<Merchant> findByApiKey(String apiKey);
    Merchant findByApiKeyAndApiSecret(String apiKey, String apiSecret);
}