package com.gateway.controllers;

import com.gateway.jobs.JobQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/test/jobs")
@CrossOrigin(origins = "*")
public class TestJobController {

    @Autowired
    private JobQueue jobQueue;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @GetMapping("/status")
    public ResponseEntity<?> getJobStatus() {
        try {
            Map<String, Object> response = new HashMap<>();
            
            long paymentQueue = jobQueue.getQueueSize("payment_queue");
            long refundQueue = jobQueue.getQueueSize("refund_queue");
            long webhookQueue = jobQueue.getQueueSize("webhook_queue");
            
            response.put("pending", paymentQueue + refundQueue + webhookQueue);
            response.put("processing", 0);
            response.put("completed", 0);
            response.put("failed", 0);
            response.put("worker_status", "running");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("error", e.getMessage())
            );
        }
    }
}
