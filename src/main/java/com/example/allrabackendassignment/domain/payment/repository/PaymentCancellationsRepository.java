package com.example.allrabackendassignment.domain.payment.repository;

import com.example.allrabackendassignment.domain.payment.entity.PaymentCancellations;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentCancellationsRepository extends JpaRepository<PaymentCancellations, Long> {
}
