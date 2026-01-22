package com.gateway.controllers;

import com.gateway.models.Payment;
import com.gateway.models.Refund;
import com.gateway.models.IdempotencyKey;
import com.gateway.models.Merchant;
import com.gateway.models.Order;
import com.gateway.repositories.PaymentRepository;
import com.gateway.repositories.RefundRepository;
import com.gateway.repositories.IdempotencyKeyRepository;
import com.gateway.repositories.MerchantRepository;
import com.gateway.repositories.OrderRepository;
import com.gateway.services.PaymentService;
import com.gateway.jobs.JobQueue;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/api/v1/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    private IdempotencyKeyRepository idempotencyKeyRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private JobQueue jobQueue;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping
    public ResponseEntity<?> createPayment(
            @RequestHeader("X-Api-Key") String apiKey,
            @RequestHeader("X-Api-Secret") String apiSecret,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody Map<String, Object> payload) {
        
        try {
            // Authenticate merchant
            Merchant merchant = merchantRepository.findByApiKeyAndApiSecret(apiKey, apiSecret);
            if (merchant == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", Map.of("code", "UNAUTHORIZED", "description", "Invalid credentials"))
                );
            }

            // Check idempotency key
            if (idempotencyKey != null) {
                String compositeKey = merchant.getId() + ":" + idempotencyKey;
                IdempotencyKey existingKey = idempotencyKeyRepository.findByKeyAndMerchantId(compositeKey, UUID.fromString(merchant.getId())).orElse(null);
                
                if (existingKey != null) {
                    if (existingKey.getExpiresAt().isAfter(LocalDateTime.now())) {
                        return ResponseEntity.status(HttpStatus.CREATED).body(
                            objectMapper.readValue(existingKey.getResponse(), Map.class)
                        );
                    } else {
                        idempotencyKeyRepository.delete(existingKey);
                    }
                }
            }

            // Create payment
            String orderId = (String) payload.get("order_id");
            String method = (String) payload.get("method");
            String vpa = (String) payload.get("vpa");

            // Get or create order
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null) {
                order = new Order();
                order.setId(orderId);
                order.setMerchant(merchant);
                order.setAmount(50000);
                order.setCurrency("INR");
                orderRepository.save(order);
            }

            Payment payment = new Payment();
            payment.setId("pay_" + generateRandomId());
            payment.setMerchantId(UUID.fromString(merchant.getId()));
            payment.setOrder(order);
            payment.setAmount(50000); // Example amount
            payment.setCurrency("INR");
            payment.setMethod(method);
            payment.setVpa(vpa);
            payment.setStatus("pending");
            payment.setCreatedAt(LocalDateTime.now());
            payment.setUpdatedAt(LocalDateTime.now());

            paymentRepository.save(payment);

            // Enqueue payment processing job
            jobQueue.enqueuePaymentJob(payment.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("id", payment.getId());
            response.put("order_id", order.getId());
            response.put("amount", payment.getAmount());
            response.put("currency", payment.getCurrency());
            response.put("method", payment.getMethod());
            response.put("vpa", payment.getVpa());
            response.put("status", payment.getStatus());
            response.put("created_at", payment.getCreatedAt());

            // Store idempotency key
            if (idempotencyKey != null) {
                String compositeKey = merchant.getId() + ":" + idempotencyKey;
                IdempotencyKey idempKey = new IdempotencyKey();
                idempKey.setKey(compositeKey);
                idempKey.setMerchantId(UUID.fromString(merchant.getId()));
                idempKey.setResponse(objectMapper.writeValueAsString(response));
                idempKey.setCreatedAt(LocalDateTime.now());
                idempKey.setExpiresAt(LocalDateTime.now().plusHours(24));
                idempotencyKeyRepository.save(idempKey);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{payment_id}/capture")
    public ResponseEntity<?> capturePayment(
            @RequestHeader("X-Api-Key") String apiKey,
            @RequestHeader("X-Api-Secret") String apiSecret,
            @PathVariable String payment_id,
            @RequestBody Map<String, Object> payload) {
        
        try {
            Merchant merchant = merchantRepository.findByApiKeyAndApiSecret(apiKey, apiSecret);
            if (merchant == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Payment payment = paymentRepository.findById(payment_id).orElse(null);
            if (payment == null || !payment.getMerchantId().equals(UUID.fromString(merchant.getId()))) {
                return ResponseEntity.notFound().build();
            }

            if (!"success".equals(payment.getStatus())) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", Map.of("code", "BAD_REQUEST_ERROR", "description", "Payment not in capturable state"))
                );
            }

            payment.setCaptured(true);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            Map<String, Object> response = new HashMap<>();
            response.put("id", payment.getId());
            response.put("order_id", payment.getOrder() != null ? payment.getOrder().getId() : null);
            response.put("amount", payment.getAmount());
            response.put("currency", payment.getCurrency());
            response.put("method", payment.getMethod());
            response.put("status", payment.getStatus());
            response.put("captured", payment.getCaptured());
            response.put("created_at", payment.getCreatedAt());
            response.put("updated_at", payment.getUpdatedAt());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{payment_id}/refunds")
    public ResponseEntity<?> createRefund(
            @RequestHeader("X-Api-Key") String apiKey,
            @RequestHeader("X-Api-Secret") String apiSecret,
            @PathVariable String payment_id,
            @RequestBody Map<String, Object> payload) {
        
        try {
            Merchant merchant = merchantRepository.findByApiKeyAndApiSecret(apiKey, apiSecret);
            if (merchant == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Payment payment = paymentRepository.findById(payment_id).orElse(null);
            if (payment == null || !payment.getMerchantId().equals(UUID.fromString(merchant.getId()))) {
                return ResponseEntity.notFound().build();
            }

            if (!"success".equals(payment.getStatus())) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", Map.of("code", "BAD_REQUEST_ERROR", "description", "Payment not refundable"))
                );
            }

            Integer refundAmount = ((Number) payload.get("amount")).intValue();
            String reason = (String) payload.get("reason");

            // Calculate total already refunded
            List<Refund> existingRefunds = refundRepository.findByPaymentIdAndStatusIn(
                payment_id, 
                List.of("pending", "processed")
            );
            int totalRefunded = existingRefunds.stream().mapToInt(Refund::getAmount).sum();

            if (totalRefunded + refundAmount > payment.getAmount()) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", Map.of("code", "BAD_REQUEST_ERROR", "description", "Refund amount exceeds available amount"))
                );
            }

            // Create refund
            Refund refund = new Refund();
            refund.setId("rfnd_" + generateRandomId());
            refund.setPaymentId(payment_id);
            refund.setMerchantId(UUID.fromString(merchant.getId()));
            refund.setAmount(refundAmount);
            refund.setReason(reason);
            refund.setStatus("pending");
            refund.setCreatedAt(LocalDateTime.now());

            refundRepository.save(refund);

            // Enqueue refund job
            jobQueue.enqueueRefundJob(refund.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("id", refund.getId());
            response.put("payment_id", refund.getPaymentId());
            response.put("amount", refund.getAmount());
            response.put("reason", refund.getReason());
            response.put("status", refund.getStatus());
            response.put("created_at", refund.getCreatedAt());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPayment(@PathVariable String id) {
        Payment payment = paymentRepository.findById(id).orElse(null);
        if (payment == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(payment);
    }

    private String generateRandomId() {
        StringBuilder sb = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < 16; i++) {
            sb.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return sb.toString();
    }
}