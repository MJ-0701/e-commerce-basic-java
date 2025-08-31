package com.example.allrabackendassignment.domain.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Getter
@Builder
@DynamicUpdate
@DynamicInsert
@Table(name = "payment_cancellations", schema = "code_interview")
public class PaymentCancellations {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id")
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", insertable = false, updatable = false)
    private PaymentTransaction paymentTransaction;

    @Column(name = "cancel_amount")
    private int cancelAmount;

    @Column(name = "cancel_reason")
    private String cancelReason;

    @Column(name = "cancel_status", columnDefinition = "varchar(100) not null default 'CANCEL_REQUESTED'")
    @Comment("'주문 취소 접수상태 -> CANCEL_REQUESTED : 취소접수, CANCEL_COMPLETE : 취소완료, CANCEL_REJECT : 취소거부'")
    private String cancelStatus;

    @Column(name = "cancel_type", columnDefinition = "varchar(50) not null default 'FULL'")
    @Comment("'FULL : 전체환불, PARTIAL : 부분환불'")
    private String cancelType;
}
