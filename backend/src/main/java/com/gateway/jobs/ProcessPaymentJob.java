package com.gateway.jobs;

import com.gateway.models.Payment;
import com.gateway.models.WebhookLog;
import com.gateway.repositories.PaymentRepository;
import com.gateway.repositories.WebhookLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Component
public class ProcessPaymentJob {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private WebhookLogRepository webhookLogRepository;

    @Autowired
    private JobQueue jobQueue;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    public void execute(String paymentId) {
        try {
            // Fetch payment record
            Payment payment = paymentRepository.findById(paymentId).orElse(null);
            if (payment == null) {
                System.out.println("Payment not found: " + paymentId);
                return;
            }

            // Simulate processing delay
            int delay = getProcessingDelay();
            Thread.sleep(delay);

            // Determine payment outcome based on method
            boolean success = determinePaymentOutcome(payment.getMethod());
            
            // Update payment status
            payment.setUpdatedAt(LocalDateTime.now());
            if (success) {
                payment.setStatus("success");
            } else {
                payment.setStatus("failed");
                payment.setErrorCode("PAYMENT_FAILED");
                payment.setErrorDescription("Payment processing failed");
            }
            paymentRepository.save(payment);

            // Enqueue webhook delivery job
            String event = success ? "payment.success" : "payment.failed";
            String payload = buildPaymentPayload(payment);
            jobQueue.enqueueWebhookJob(payment.getMerchantId().toString(), event, payload);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getProcessingDelay() {
        String testMode = System.getenv("TEST_MODE");
        if ("true".equals(testMode)) {
            String testDelay = System.getenv("TEST_PROCESSING_DELAY");
            return testDelay != null ? Integer.parseInt(testDelay) : 1000;
        }
        // Random delay between 5-10 seconds
        return 5000 + random.nextInt(5000);
    }

    private boolean determinePaymentOutcome(String method) {
        String testMode = System.getenv("TEST_MODE");
        if ("true".equals(testMode)) {
            String testSuccess = System.getenv("TEST_PAYMENT_SUCCESS");
            return testSuccess == null || "true".equals(testSuccess);
        }

        // UPI: 90% success, Card: 95% success
        double successRate = "upi".equals(method) ? 0.90 : 0.95;
        return random.nextDouble() < successRate;
    }

    private String buildPaymentPayload(Payment payment) {
        try {
            Map<String, Object> data = new HashMap<>();
            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("id", payment.getId());
            paymentData.put("order_id", payment.getOrder() != null ? payment.getOrder().getId() : null);
            paymentData.put("amount", payment.getAmount());
            paymentData.put("currency", payment.getCurrency());
            paymentData.put("method", payment.getMethod());
            if (payment.getVpa() != null) paymentData.put("vpa", payment.getVpa());
            paymentData.put("status", payment.getStatus());
            paymentData.put("created_at", payment.getCreatedAt());

            data.put("payment", paymentData);

            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }
}