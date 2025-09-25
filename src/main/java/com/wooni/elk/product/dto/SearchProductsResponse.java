package com.wooni.elk.product.dto;

import java.util.List;

public record SearchProductsResponse(
        long total,
        int page,
        int size,
        List<SearchProductItem> items
) {}
