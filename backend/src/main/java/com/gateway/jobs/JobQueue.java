package com.gateway.jobs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;

@Component
public class JobQueue {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void enqueuePaymentJob(String paymentId) {
        JobPayload payload = new JobPayload();
        payload.setType("ProcessPaymentJob");
        payload.setId(paymentId);

        try {
            String json = objectMapper.writeValueAsString(payload);
            redisTemplate.opsForList().rightPush("payment_queue", json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void enqueueRefundJob(String refundId) {
        JobPayload payload = new JobPayload();
        payload.setType("ProcessRefundJob");
        payload.setId(refundId);

        try {
            String json = objectMapper.writeValueAsString(payload);
            redisTemplate.opsForList().rightPush("refund_queue", json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void enqueueWebhookJob(String merchantId, String eventType, String payloadJson) {
        JobPayload payload = new JobPayload();
        payload.setType("DeliverWebhookJob");
        payload.setMerchantId(merchantId);
        payload.setEvent(eventType);
        payload.setPayload(payloadJson);
        payload.setAttempts(0);

        try {
            String json = objectMapper.writeValueAsString(payload);
            redisTemplate.opsForList().rightPush("webhook_queue", json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getQueueSize(String queueName) {
        Long size = redisTemplate.opsForList().size(queueName);
        return size != null ? size : 0;
    }
}
