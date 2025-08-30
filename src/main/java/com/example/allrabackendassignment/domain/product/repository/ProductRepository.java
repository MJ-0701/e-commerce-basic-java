package com.example.allrabackendassignment.domain.product.repository;

import com.example.allrabackendassignment.domain.product.entity.Product;
import com.example.allrabackendassignment.domain.product.repository.custom.ProductCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductCustomRepository {
}
