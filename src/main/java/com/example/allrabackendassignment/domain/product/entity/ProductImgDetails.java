package com.example.allrabackendassignment.domain.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@DynamicInsert
@DynamicUpdate
@Table(name = "product_img_details", schema = "code_interview")
public class ProductImgDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", columnDefinition = "varchar(10)", nullable = false)
    private ProductDetailImgType productDetailImgType;

    @Column(name = "img_url")
    private String imgUrl;

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            mappedBy = "productImgDetails"
    )
    private List<ProductImgDetailsMapping> productImgDetailsMapping;

}
