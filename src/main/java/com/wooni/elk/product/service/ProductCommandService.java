package com.wooni.elk.product.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wooni.elk.product.dto.AddProductRequest;
import com.wooni.elk.product.dto.AddProductsRequest;
import com.wooni.elk.product.dto.ProductDocument;
import com.wooni.elk.product.entity.OutboxEvent;
import com.wooni.elk.product.entity.ProductEntity;
import com.wooni.elk.product.mapper.ProductMapper;
import com.wooni.elk.product.repository.OutboxRepository;
import com.wooni.elk.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductCommandService {

    private final ProductRepository productRepo;
    private final OutboxRepository outboxRepo;
    private final ProductMapper mapper;
    private final ObjectMapper om = new ObjectMapper();

    public void upsertProduct(AddProductRequest r) {
        Long id = Long.parseLong(r.productId());
        ProductEntity e = productRepo.findById(id).orElseGet(ProductEntity::new);

        e.setId(id);
        e.setTitle(r.title());
        e.setBrand(r.brand());
        e.setBrandId(r.brandId());
        e.setCategoryId(r.categoryId());
        e.setPrice(r.price());
        e.setOnSale(Boolean.TRUE.equals(r.onSale()));
        e.setDescription(r.description());
        e.setUpdatedAt(Instant.now());
        productRepo.save(e);

        // Outbox 이벤트 (UPSERT)
        ProductDocument doc = mapper.toDoc(e);
        String payloadJson = toJson(doc);

        OutboxEvent ev = new OutboxEvent();
        ev.setAggregateType("PRODUCT");
        ev.setAggregateId(String.valueOf(e.getId()));
        ev.setEventType("UPSERT");
        ev.setPayloadJson(payloadJson);
        ev.setCreatedAt(Instant.now());
        ev.setProcessed(false);
        outboxRepo.save(ev);
    }

    public void upsertProducts(AddProductsRequest req) {
        for (AddProductRequest r : req.items()) upsertProduct(r);
    }

    public void deleteProduct(Long productId) {
        productRepo.deleteById(productId);

        OutboxEvent ev = new OutboxEvent();
        ev.setAggregateType("PRODUCT");
        ev.setAggregateId(String.valueOf(productId));
        ev.setEventType("DELETE");
        ev.setPayloadJson("{}");
        ev.setCreatedAt(Instant.now());
        ev.setProcessed(false);
        outboxRepo.save(ev);
    }

    private String toJson(Object o){
        try { return om.writeValueAsString(o); }
        catch (Exception e){ throw new RuntimeException(e); }
    }
}

