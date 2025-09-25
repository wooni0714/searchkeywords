package com.wooni.elk.product.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductDocument {
    private String product_id;
    private String title;
    private String brand;
    private String brand_id;
    private String category_id;
    private Integer price;
    private Boolean on_sale;
    private String description;
    private String launch_date;
}

