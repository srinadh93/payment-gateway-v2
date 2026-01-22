package com.gateway.workers;

import com.gateway.jobs.ProcessRefundJob;
import com.gateway.jobs.JobPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@EnableScheduling
public class RefundWorker {

    @Autowired
    private ProcessRefundJob processRefundJob;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(fixedRate = 1000) // Poll every 1 second
    public void processRefunds() {
        try {
            String jobJson = redisTemplate.opsForList().leftPop("refund_queue");
            if (jobJson == null) return;

            JobPayload job = objectMapper.readValue(jobJson, JobPayload.class);
            processRefundJob.execute(job.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
