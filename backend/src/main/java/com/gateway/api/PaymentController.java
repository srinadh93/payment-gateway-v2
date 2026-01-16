package com.gateway.api;

import com.gateway.entity.Payment;
import com.gateway.jobs.ProcessPaymentJob;
import com.gateway.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentRepository paymentRepository;

    @PostMapping
    public Map<String, Object> createPayment(@RequestBody Map<String, Object> request) {
        try {
            Double amount = Double.valueOf(request.get("amount").toString());
            String webhookUrl = request.containsKey("webhookUrl") ? request.get("webhookUrl").toString() : null;
            
            String paymentId = "pay_" + UUID.randomUUID().toString();

            // Save PENDING with Webhook URL
            Payment payment = new Payment(paymentId, amount, "pending", webhookUrl);
            paymentRepository.save(payment);

            // Send to Redis
            ProcessPaymentJob job = new ProcessPaymentJob(paymentId);
            String jobJson = objectMapper.writeValueAsString(job);
            redisTemplate.opsForList().leftPush("payment_queue", jobJson);

            return Map.of("id", paymentId, "status", "pending", "message", "Processing started");

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", "Internal Server Error");
        }
    }

    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
}