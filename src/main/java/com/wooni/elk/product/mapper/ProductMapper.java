package com.wooni.elk.product.mapper;

import com.wooni.elk.product.dto.ProductDocument;
import com.wooni.elk.product.entity.ProductEntity;
import org.springframework.stereotype.Component;

// Entity ↔ ES 문서
@Component
public class ProductMapper {
    public ProductDocument toDoc(ProductEntity e) {
        ProductDocument d = new ProductDocument();
        d.setProduct_id(String.valueOf(e.getId()));
        d.setTitle(n(e.getTitle()));
        d.setBrand(n(e.getBrand()));
        d.setBrand_id(n(e.getBrandId()));
        d.setCategory_id(n(e.getCategoryId()));
        d.setPrice(e.getPrice());
        d.setOn_sale(Boolean.TRUE.equals(e.getOnSale()));
        d.setDescription(n(e.getDescription()));
        return d;
    }
    private String n(String s){
        return s==null? "": s;
    }
}