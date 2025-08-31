package com.example.allrabackendassignment.domain.payment.repository;

import com.example.allrabackendassignment.domain.payment.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
}
