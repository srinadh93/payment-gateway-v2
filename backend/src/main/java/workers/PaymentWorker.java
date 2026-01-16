package com.gateway.workers;

import com.gateway.entity.Payment;
import com.gateway.jobs.ProcessPaymentJob;
import com.gateway.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate; // <--- NEW IMPORT

import java.util.concurrent.TimeUnit;
import java.util.Random;
import java.util.Optional;
import java.util.Map;

@Component
public class PaymentWorker {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentRepository paymentRepository;

    private final Random random = new Random();
    private final RestTemplate restTemplate = new RestTemplate(); // <--- Tool to send Webhooks

    @Scheduled(fixedDelay = 100)
    public void processJobs() {
        try {
            String jobJson = redisTemplate.opsForList().rightPop("payment_queue", 1, TimeUnit.SECONDS);
            
            if (jobJson != null) {
                ProcessPaymentJob job = objectMapper.readValue(jobJson, ProcessPaymentJob.class);
                processPayment(job.getPaymentId());
            }
        } catch (Exception e) {
            System.err.println("Error processing job: " + e.getMessage());
        }
    }

    private void processPayment(String paymentId) {
        try {
            System.out.println("Processing payment: " + paymentId);

            Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
            if (paymentOpt.isEmpty()) return;

            Payment payment = paymentOpt.get();

            // Simulate Bank Delay
            Thread.sleep(2000);

            // Determine Success/Failure
            boolean isSuccess = random.nextDouble() > 0.20;
            String status = isSuccess ? "success" : "failed";
            
            payment.setStatus(status);
            paymentRepository.save(payment); // Save to DB

            System.out.println("Payment " + paymentId + " " + status.toUpperCase());

            // <--- NEW: Send Webhook Notification if URL exists
            if (payment.getWebhookUrl() != null && !payment.getWebhookUrl().isEmpty()) {
                sendWebhook(payment);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendWebhook(Payment payment) {
        try {
            System.out.println("Sending Webhook to: " + payment.getWebhookUrl());
            
            Map<String, Object> payload = Map.of(
                "id", payment.getId(),
                "status", payment.getStatus(),
                "amount", payment.getAmount()
            );
            
            // Send POST request
            restTemplate.postForObject(payment.getWebhookUrl(), payload, String.class);
            System.out.println("Webhook sent successfully!");
            
        } catch (Exception e) {
            System.err.println("Failed to send webhook: " + e.getMessage());
        }
    }
}