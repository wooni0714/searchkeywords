package com.wooni.elk.product.dto;

public record SearchProductItem(
        String productId,
        String title,
        String brand,
        String categoryId,
        Integer price,
        Double score,
        String highlightTitle
) {}
