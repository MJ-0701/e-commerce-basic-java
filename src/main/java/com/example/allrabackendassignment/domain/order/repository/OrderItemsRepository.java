package com.example.allrabackendassignment.domain.order.repository;

import com.example.allrabackendassignment.domain.order.entity.OrderItems;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemsRepository extends JpaRepository<OrderItems, Long> {
}
