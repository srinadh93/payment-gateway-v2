package com.gateway.repository;

import com.gateway.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    // This interface automatically gives us methods like:
    // .save(), .findById(), .findAll()
}