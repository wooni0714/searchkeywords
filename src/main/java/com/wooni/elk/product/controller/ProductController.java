package com.wooni.elk.product.controller;

import com.wooni.elk.product.dto.*;
import com.wooni.elk.product.service.ProductCommandService;
import com.wooni.elk.product.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductCommandService commandService;
    private final ProductSearchService searchService;

    @PostMapping
    public ResponseEntity<Void> add(@RequestBody AddProductRequest req) {
        commandService.upsertProduct(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/bulk")
    public ResponseEntity<Void> addBulk(@RequestBody AddProductsRequest req) {
        commandService.upsertProducts(req);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> delete(@PathVariable Long productId) {
        commandService.deleteProduct(productId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{productId}")
    public ResponseEntity<GetProductResponse> get(@PathVariable String productId) throws IOException {
        var resp = searchService.getById(productId);
        return resp==null ? ResponseEntity.notFound().build() : ResponseEntity.ok(resp);
    }

    @PostMapping("/_search")
    public SearchProductsResponse search(@RequestBody SearchProductsRequest req) throws IOException {
        return searchService.search(req);
    }
}
