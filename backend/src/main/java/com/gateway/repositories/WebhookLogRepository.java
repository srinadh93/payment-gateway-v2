package com.gateway.repositories;

import com.gateway.models.WebhookLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WebhookLogRepository extends JpaRepository<WebhookLog, String> {
    Page<WebhookLog> findByMerchantId(UUID merchantId, Pageable pageable);
    List<WebhookLog> findByStatusAndNextRetryAtBeforeOrderByNextRetryAtAsc(String status, LocalDateTime now);
    Optional<WebhookLog> findById(String id);
    List<WebhookLog> findByMerchantIdAndEvent(UUID merchantId, String event);
}
