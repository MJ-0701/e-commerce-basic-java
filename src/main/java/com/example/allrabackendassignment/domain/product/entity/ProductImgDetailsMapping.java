package com.example.allrabackendassignment.domain.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@DynamicInsert
@DynamicUpdate
@Table(name = "product_img_details_mapping")
public class ProductImgDetailsMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    @Column(name = "product_img_details_id")
    private Long productImgDetailsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_img_details_id", insertable = false, updatable = false)
    private ProductImgDetails productImgDetails;
}
