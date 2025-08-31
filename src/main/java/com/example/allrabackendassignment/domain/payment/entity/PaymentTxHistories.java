package com.example.allrabackendassignment.domain.payment.entity;

import com.fasterxml.jackson.annotation.JsonTypeId;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;

@Entity
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Getter
@Builder
@DynamicUpdate
@DynamicInsert
@Table(name = "payment_tx_histories", schema = "code_interview")
public class PaymentTxHistories {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", insertable = false, updatable = false)
    private PaymentTransaction paymentTransaction;

    @Column(name = "status", columnDefinition = "varchar(50) not null default 'REQUEST'")
    private PaymentStatus status;
}
