package com.gateway.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    private String id;

    private Double amount;
    private String status; // pending, success, failed
    private String webhookUrl; // <--- This is the new field
    private LocalDateTime createdAt;

    public Payment() {}

    public Payment(String id, Double amount, String status, String webhookUrl) {
        this.id = id;
        this.amount = amount;
        this.status = status;
        this.webhookUrl = webhookUrl;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getWebhookUrl() { return webhookUrl; }
    public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}