package com.gateway.workers;

import com.gateway.jobs.DeliverWebhookJob;
import com.gateway.jobs.JobPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@EnableScheduling
public class WebhookWorker {

    @Autowired
    private DeliverWebhookJob deliverWebhookJob;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(fixedRate = 1000) // Poll every 1 second
    public void deliverWebhooks() {
        try {
            String jobJson = redisTemplate.opsForList().leftPop("webhook_queue");
            if (jobJson == null) return;

            JobPayload job = objectMapper.readValue(jobJson, JobPayload.class);
            deliverWebhookJob.execute(job.getMerchantId(), job.getEvent(), job.getPayload());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}