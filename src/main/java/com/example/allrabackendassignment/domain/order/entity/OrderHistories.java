package com.example.allrabackendassignment.domain.order.entity;

import com.example.allrabackendassignment.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Getter
@Builder
@DynamicUpdate
@DynamicInsert
@Table(name = "order_histories", schema = "code_interview")
public class OrderHistories extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Orders order;

    @Column(name = "status", columnDefinition = "varchar(20) default 'CREATED'")
    private OrderStatus status;

    @Column(name = "message", columnDefinition = "varchar(20) default '주문 생성'")
    private String message;

    @Column(name = "changed_by", columnDefinition = "varchar(20) default 'SYSTEM'")
    private String changedBy;

}
