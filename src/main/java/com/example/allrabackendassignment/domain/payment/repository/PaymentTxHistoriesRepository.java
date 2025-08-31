package com.example.allrabackendassignment.domain.payment.repository;

import com.example.allrabackendassignment.domain.payment.entity.PaymentTxHistories;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentTxHistoriesRepository extends JpaRepository<PaymentTxHistories, Long> {
}
