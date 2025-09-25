package com.wooni.elk.product.dto;

import java.util.List;

public record AddProductsRequest(List<AddProductRequest> items) {

}