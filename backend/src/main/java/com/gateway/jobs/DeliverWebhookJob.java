package com.gateway.jobs;

import com.gateway.models.Merchant;
import com.gateway.models.WebhookLog;
import com.gateway.repositories.MerchantRepository;
import com.gateway.repositories.WebhookLogRepository;
import com.gateway.services.WebhookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
public class DeliverWebhookJob {

    @Autowired
    private WebhookLogRepository webhookLogRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private WebhookService webhookService;

    public void execute(String merchantId, String eventType, String payload) {
        try {
            UUID uuid = UUID.fromString(merchantId);
            Optional<Merchant> merchantOpt = merchantRepository.findById(uuid.toString());
            
            if (merchantOpt.isEmpty() || merchantOpt.get().getWebhookUrl() == null) {
                System.out.println("Merchant or webhook URL not configured");
                return;
            }

            Merchant merchant = merchantOpt.get();
            WebhookLog log = new WebhookLog();
            log.setMerchantId(uuid);
            log.setEvent(eventType);
            log.setPayload(payload);
            log.setStatus("pending");
            log.setAttempts(0);
            webhookLogRepository.save(log);

            // Deliver webhook
            deliverWebhook(log, merchant, payload);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deliverWebhook(WebhookLog log, Merchant merchant, String payload) {
        try {
            int statusCode = webhookService.sendWebhook(
                merchant.getWebhookUrl(),
                merchant.getWebhookSecret(),
                payload
            );

            log.setLastAttemptAt(LocalDateTime.now());
            log.setAttempts(log.getAttempts() + 1);
            log.setResponseCode(statusCode);

            if (statusCode >= 200 && statusCode < 300) {
                log.setStatus("success");
            } else {
                log.setStatus("pending");
                if (log.getAttempts() < 5) {
                    log.setNextRetryAt(calculateNextRetryTime(log.getAttempts()));
                } else {
                    log.setStatus("failed");
                }
            }

            webhookLogRepository.save(log);

        } catch (Exception e) {
            e.printStackTrace();
            log.setLastAttemptAt(LocalDateTime.now());
            log.setAttempts(log.getAttempts() + 1);
            log.setStatus("pending");

            if (log.getAttempts() < 5) {
                log.setNextRetryAt(calculateNextRetryTime(log.getAttempts()));
            } else {
                log.setStatus("failed");
            }

            webhookLogRepository.save(log);
        }
    }

    private LocalDateTime calculateNextRetryTime(int attempts) {
        boolean testMode = "true".equals(System.getenv("WEBHOOK_RETRY_INTERVALS_TEST"));
        LocalDateTime now = LocalDateTime.now();

        if (testMode) {
            // Test intervals: 0s, 5s, 10s, 15s, 20s
            int[] intervals = {0, 5, 10, 15, 20};
            if (attempts < intervals.length) {
                return now.plusSeconds(intervals[attempts]);
            }
            return now.plusSeconds(20);
        } else {
            // Production intervals: 1min, 5min, 30min, 2hr
            int[] intervals = {60, 300, 1800, 7200};
            if (attempts < intervals.length) {
                return now.plusSeconds(intervals[attempts]);
            }
            return now.plusSeconds(7200);
        }
    }
}