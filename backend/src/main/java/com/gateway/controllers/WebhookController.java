package com.gateway.controllers;

import com.gateway.models.WebhookLog;
import com.gateway.models.Merchant;
import com.gateway.repositories.WebhookLogRepository;
import com.gateway.repositories.MerchantRepository;
import com.gateway.jobs.JobQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/webhooks")
@CrossOrigin(origins = "*")
public class WebhookController {

    @Autowired
    private WebhookLogRepository webhookLogRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private JobQueue jobQueue;

    @GetMapping
    public ResponseEntity<?> listWebhookLogs(
            @RequestHeader("X-Api-Key") String apiKey,
            @RequestHeader("X-Api-Secret") String apiSecret,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        
        try {
            Merchant merchant = merchantRepository.findByApiKeyAndApiSecret(apiKey, apiSecret);
            if (merchant == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Pageable pageable = PageRequest.of(offset / limit, limit);
            Page<WebhookLog> page = webhookLogRepository.findByMerchantId(
                UUID.fromString(merchant.getId()),
                pageable
            );

            List<Map<String, Object>> logs = page.getContent().stream()
                .map(log -> {
                    Map<String, Object> logMap = new HashMap<>();
                    logMap.put("id", log.getId());
                    logMap.put("event", log.getEvent());
                    logMap.put("status", log.getStatus());
                    logMap.put("attempts", log.getAttempts());
                    logMap.put("created_at", log.getCreatedAt());
                    if (log.getLastAttemptAt() != null) {
                        logMap.put("last_attempt_at", log.getLastAttemptAt());
                    }
                    if (log.getResponseCode() != null) {
                        logMap.put("response_code", log.getResponseCode());
                    }
                    return logMap;
                })
                .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("data", logs);
            response.put("total", page.getTotalElements());
            response.put("limit", limit);
            response.put("offset", offset);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{webhook_id}/retry")
    public ResponseEntity<?> retryWebhook(
            @RequestHeader("X-Api-Key") String apiKey,
            @RequestHeader("X-Api-Secret") String apiSecret,
            @PathVariable String webhook_id) {
        
        try {
            Merchant merchant = merchantRepository.findByApiKeyAndApiSecret(apiKey, apiSecret);
            if (merchant == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            WebhookLog log = webhookLogRepository.findById(webhook_id).orElse(null);
            if (log == null || !log.getMerchantId().equals(UUID.fromString(merchant.getId()))) {
                return ResponseEntity.notFound().build();
            }

            // Reset for retry
            log.setStatus("pending");
            log.setAttempts(0);
            log.setNextRetryAt(LocalDateTime.now());
            webhookLogRepository.save(log);

            // Re-enqueue for delivery
            jobQueue.enqueueWebhookJob(
                log.getMerchantId().toString(),
                log.getEvent(),
                log.getPayload()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("id", log.getId());
            response.put("status", log.getStatus());
            response.put("message", "Webhook retry scheduled");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
