package com.example.allrabackendassignment.domain.order.entity;

import com.example.allrabackendassignment.domain.user.entity.User;
import com.example.allrabackendassignment.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Getter
@Builder
@DynamicUpdate
@DynamicInsert
@Table(name = "orders", schema = "code_interview")
public class Orders extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", unique = true, nullable = false)
    private GeneratedUUID orderId;

    @Column(name = "order_name", nullable = false)
    private String orderName;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "order_status", columnDefinition = "varchar(20) default 'CREATED'")
    private OrderStatus orderStatus;

    @Column(name = "total_amount", columnDefinition = "int unsigned not null")
    private int totalAmount;

    @Column(name = "paid_amount", columnDefinition = "int unsigned not null")
    private int paidAmount;

    @Column(name = "canceled_amount", columnDefinition = "int unsigned default 0")
    private int canceledAmount;

    @Column(name = "is_fully_canceled", columnDefinition = "boolean default false")
    private boolean isFullyCanceled;

    @OneToMany(
            fetch = FetchType.LAZY,
            orphanRemoval = true,
            cascade = CascadeType.ALL,
            mappedBy = "orders"
    )
    private List<OrderItems> orderItems;


    public void updateOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}
