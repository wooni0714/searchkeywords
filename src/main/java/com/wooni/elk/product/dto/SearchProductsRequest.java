package com.wooni.elk.product.dto;

import java.util.List;

public record SearchProductsRequest(
        String query,
        List<String> brandIds,
        List<String> categoryIds,
        Boolean onSale,
        Integer page,
        Integer size
) {}