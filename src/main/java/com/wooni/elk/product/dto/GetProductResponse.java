package com.wooni.elk.product.dto;

public record GetProductResponse(
        String productId,
        String title,
        String brand,
        String brandId,
        String categoryId,
        Integer price,
        Boolean onSale,
        String description
) {}