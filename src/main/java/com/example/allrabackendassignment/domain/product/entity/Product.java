package com.example.allrabackendassignment.domain.product.entity;

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
@Table(name = "product", schema = "code_interview")
public class Product extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    private String productName;

    private int productPrice;

    @Column(name = "category_id")
    private Long categoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private ProductCategory category;

    @Column(name = "img_details_mapping")
    private Long imgDetailsMappingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "img_details_mapping", insertable = false, updatable = false)
    private ProductImgDetailsMapping imgDetailsMapping;




}
