package com.wooni.elk.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "product")
@Getter
@Setter
public class ProductEntity {
    @Id
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String brand;

    private String brandId;

    private String categoryId;

    private Integer price;

    private Boolean onSale;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)  private Instant updatedAt;
}