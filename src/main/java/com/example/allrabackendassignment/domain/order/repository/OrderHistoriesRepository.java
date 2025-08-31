package com.example.allrabackendassignment.domain.order.repository;

import com.example.allrabackendassignment.domain.order.entity.OrderHistories;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderHistoriesRepository extends JpaRepository<OrderHistories, Long> {
}
