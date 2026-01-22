package com.gateway.jobs;

import com.gateway.models.Payment;
import com.gateway.models.Refund;
import com.gateway.repositories.PaymentRepository;
import com.gateway.repositories.RefundRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Component
public class ProcessRefundJob {

    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private JobQueue jobQueue;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    public void execute(String refundId) {
        try {
            // Fetch refund record
            Refund refund = refundRepository.findById(refundId).orElse(null);
            if (refund == null) {
                System.out.println("Refund not found: " + refundId);
                return;
            }

            // Fetch and verify payment
            Payment payment = paymentRepository.findById(refund.getPaymentId()).orElse(null);
            if (payment == null || !"success".equals(payment.getStatus())) {
                System.out.println("Payment not in refundable state");
                return;
            }

            // Simulate refund processing delay (3-5 seconds)
            int delay = 3000 + random.nextInt(2000);
            Thread.sleep(delay);

            // Update refund status
            refund.setStatus("processed");
            refund.setProcessedAt(LocalDateTime.now());
            refundRepository.save(refund);

            // Enqueue webhook delivery job
            String payload = buildRefundPayload(refund);
            jobQueue.enqueueWebhookJob(refund.getMerchantId().toString(), "refund.processed", payload);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String buildRefundPayload(Refund refund) {
        try {
            Map<String, Object> data = new HashMap<>();
            Map<String, Object> refundData = new HashMap<>();
            refundData.put("id", refund.getId());
            refundData.put("payment_id", refund.getPaymentId());
            refundData.put("amount", refund.getAmount());
            refundData.put("reason", refund.getReason());
            refundData.put("status", refund.getStatus());
            refundData.put("created_at", refund.getCreatedAt());

            data.put("refund", refundData);

            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }
}