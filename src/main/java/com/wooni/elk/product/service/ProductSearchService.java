package com.wooni.elk.product.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.wooni.elk.common.Const;
import com.wooni.elk.product.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSearchService {

    private final ElasticsearchClient client;

    public GetProductResponse getById(String productId) throws IOException {
        GetResponse<ProductDocument> resp = client.get(g -> g
                .index(Const.PRODUCTS)
                .id(productId), ProductDocument.class);
        if (!resp.found()) return null;

        ProductDocument s = resp.source();
        assert s != null;
        return new GetProductResponse(
                s.getProduct_id(),
                s.getTitle(),
                s.getBrand(),
                s.getBrand_id(),
                s.getCategory_id(),
                s.getPrice(),
                s.getOn_sale(),
                s.getDescription()
        );
    }

    public SearchProductsResponse search(SearchProductsRequest req) throws IOException {
        int page = req.page()==null?0:Math.max(0, req.page());
        int size = req.size()==null?20:Math.max(1, req.size());
        int from = page * size;

        SearchRequest esReq = SearchRequest.of(s -> s
                .index(Const.PRODUCTS)
                .from(from).size(size)
                .query(q -> q.bool(b -> {
                    b.must(m -> m.multiMatch(mm -> mm
                            .query(req.query())
                            .fields("title^3").fields("brand^2").fields("description")
                            .type(TextQueryType.BestFields)
                    ));

                    if (req.brandIds()!=null && !req.brandIds().isEmpty())
                        b.filter(f -> f.terms(t -> t
                                .field("brand_id")
                                .terms(tf -> tf.value(toFieldValues(req.brandIds())))
                        ));
                    if (req.categoryIds()!=null && !req.categoryIds().isEmpty())
                        b.filter(f -> f.terms(t -> t
                                .field("category_id")
                                .terms(tf -> tf.value(toFieldValues(req.categoryIds())))
                        ));
                    if (Boolean.TRUE.equals(req.onSale()))
                        b.filter(f -> f.term(t -> t
                                .field("on_sale").value(true))
                        );
                    return b;
                }))
                .highlight(h -> h.fields("title",
                        hf -> hf
                                .preTags("<em>")
                                .postTags("</em>").
                                numberOfFragments(0)))
        );

        SearchResponse<ProductDocument> esResp = client.search(esReq, ProductDocument.class);
        long total = esResp.hits().total()!=null ? esResp.hits().total().value() : 0;

        List<SearchProductItem> items = esResp.hits().hits().stream().map(h -> {
            ProductDocument s = h.source();
            String hl = (h.highlight()!=null && h.highlight().get("title")!=null && !h.highlight().get("title").isEmpty())
                    ? h.highlight().get("title").get(0) : null;
            assert s != null;
            return new SearchProductItem(
                    s.getProduct_id(), s.getTitle(), s.getBrand(), s.getCategory_id(),
                    s.getPrice(), h.score(), hl
            );
        }).toList();

        return new SearchProductsResponse(total, page, size, items);
    }

    private static List<FieldValue> toFieldValues(List<String> values) {
        return values.stream().map(FieldValue::of).toList();
    }
}

