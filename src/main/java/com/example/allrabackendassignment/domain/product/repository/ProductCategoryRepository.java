package com.example.allrabackendassignment.domain.product.repository;

import com.example.allrabackendassignment.domain.product.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
}
