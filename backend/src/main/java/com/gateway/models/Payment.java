package com.gateway.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payment {

    @Id
    private String id; // Generated like "pay_xyz789"

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String status; // pending, success, failed

    @Column(nullable = false)
    private String method; // upi, card

    // Optional Details based on method
    private String vpa; // for UPI
    private String cardNetwork; // visa, mastercard
    private String cardLast4;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // New fields for error tracking
    @Column(name = "error_description")
    private String errorDescription;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "captured")
    private Boolean captured = false;

    // Relationship: Payment belongs to an Order
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    public Payment() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.captured = false;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}