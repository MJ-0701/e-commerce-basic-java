package com.example.allrabackendassignment.domain.product.entity;

import com.example.allrabackendassignment.domain.cart.entity.CartItems;
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
@Table(name = "product", schema = "code_interview")
public class Product extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(name = "product_name", columnDefinition = "varchar(100) not null")
    private String productName;

    @Column(name = "product_price", columnDefinition = "int unsigned not null default 1000")
    private int productPrice;

    @Column(name = "product_thumbnail", columnDefinition = "text default null")
    private String productThumbnail;

    @Column(name = "category_id")
    private Long categoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private ProductCategory category;

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive;

    @Column(name = "stock", columnDefinition = "int unsigned default 100")
    private int stock;

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            mappedBy = "product"
    )
    private List<ProductImgDetailsMapping> productImgDetailsMapping;

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            mappedBy = "product"
    )
    private List<CartItems> cartItems;


    public void decreaseStock(int quantity) {
        this.stock -= quantity;
    }

}
