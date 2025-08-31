package com.example.allrabackendassignment.domain.order.repository;

import com.example.allrabackendassignment.domain.order.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdersRepository extends JpaRepository<Orders, Long> {
}
