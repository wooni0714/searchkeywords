package com.wooni.elk.product.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wooni.elk.common.Const;
import com.wooni.elk.product.dto.ProductDocument;
import com.wooni.elk.product.entity.OutboxEvent;
import com.wooni.elk.product.repository.OutboxRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxWorker {

    private final OutboxRepository outboxRepo;
    private final ElasticsearchClient es;

    @Transactional
    @Scheduled(fixedDelay = 500)
    public void drain() {
        List<OutboxEvent> batch = outboxRepo.findUnprocessed(PageRequest.of(0, 500));
        if (batch.isEmpty()) return;

        BulkRequest.Builder bulk = new BulkRequest.Builder();

        for (OutboxEvent ev : batch) {
            String id = ev.getAggregateId();
            if ("UPSERT".equals(ev.getEventType())) {
                ProductDocument doc = fromJson(ev.getPayloadJson());
                bulk.operations(op -> op
                        .index(idx -> idx.index(Const.PRODUCTS).id(id).document(doc)));
            } else if ("DELETE".equals(ev.getEventType())) {
                bulk.operations(op -> op
                        .delete(d -> d.index(Const.PRODUCTS).id(id)));
            }
        }

        try {
            BulkResponse resp = es.bulk(bulk.build());
            if (resp.errors()) {
                resp.items().forEach(i -> {
                    if (i.error() != null) log.warn("ES bulk err id={}, reason={}", i.id(), i.error().reason());
                });
            }
            outboxRepo.markProcessed(batch.stream().map(OutboxEvent::getId).toList());
        } catch (IOException e) {
            log.error("Outbox bulk failed: {}", e.getMessage());
            // 트랜잭션 롤백으로 processed 업데이트는 보류 → 다음 주기 재시도
            throw new RuntimeException(e);
        }
    }

    private ProductDocument fromJson(String json){
        try { return new ObjectMapper().readValue(json, ProductDocument.class); }
        catch (Exception e){ throw new RuntimeException(e); }
    }
}

