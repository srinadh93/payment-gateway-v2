package com.gateway.controllers;

import com.gateway.models.Refund;
import com.gateway.models.Merchant;
import com.gateway.repositories.RefundRepository;
import com.gateway.repositories.MerchantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/refunds")
@CrossOrigin(origins = "*")
public class RefundController {

    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @GetMapping("/{refund_id}")
    public ResponseEntity<?> getRefund(
            @RequestHeader("X-Api-Key") String apiKey,
            @RequestHeader("X-Api-Secret") String apiSecret,
            @PathVariable String refund_id) {
        
        try {
            Merchant merchant = merchantRepository.findByApiKeyAndApiSecret(apiKey, apiSecret);
            if (merchant == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Refund refund = refundRepository.findById(refund_id).orElse(null);
            if (refund == null || !refund.getMerchantId().equals(UUID.fromString(merchant.getId()))) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("id", refund.getId());
            response.put("payment_id", refund.getPaymentId());
            response.put("amount", refund.getAmount());
            response.put("reason", refund.getReason());
            response.put("status", refund.getStatus());
            response.put("created_at", refund.getCreatedAt());
            if (refund.getProcessedAt() != null) {
                response.put("processed_at", refund.getProcessedAt());
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}