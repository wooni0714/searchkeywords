package com.wooni.elk.keywords.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import com.wooni.elk.common.keywordFilter.KeywordFilterService;
import com.wooni.elk.common.exception.BusinessException;
import com.wooni.elk.keywords.dto.PopularKeywordResponse;
import com.wooni.elk.keywords.dto.SearchKeywordRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.wooni.elk.common.Const.INDEX_NAME;
import static com.wooni.elk.common.exception.ResultCode.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchKeywordService {
    private final KeywordFilterService keywordFilterService;
    private final ElasticsearchClient client;

    public void logSearchKeyword(SearchKeywordRequest searchRequest) throws BusinessException {
        boolean keywordFilter = keywordFilterService.hasForbiddenWord(searchRequest.keyword());
        if (keywordFilter) {
            throw new BusinessException(CODE_9100);
        }
        log.info("event-keyword-search, {}", searchRequest.keyword());
    }

    public List<PopularKeywordResponse> getTopKeywordsWithin24Hours() throws BusinessException {
        int size = 10;
        try {
            SearchRequest req = SearchRequest.of(s -> s
                    .index(INDEX_NAME)
                    .size(0)
                    .query(q -> q
                            .bool(b -> b
                                    .filter(f -> f
                                            .range(r -> r
                                                    .field("searchDate")
                                                    .gte(JsonData.of("now-24h"))
                                                    .lte(JsonData.of("now"))
                                            )
                                    )
                            )
                    )
                    .aggregations("popular_keywords", a -> a
                            .terms(t -> t
                                    .field("searchKeyword")
                                    .size(size)
                            )
                    )
            );

            SearchResponse<Void> resp = client.search(req, Void.class);

            Map<String, Aggregate> aggs = resp.aggregations();
            Aggregate agg = aggs.get("popular_keywords");
            if (agg == null) {
                throw new BusinessException(CODE_9101);
            }

            StringTermsAggregate terms = agg.sterms();

            return terms.buckets().array().stream()
                    .map(bucket -> new PopularKeywordResponse(
                            bucket.key().stringValue(),
                            bucket.docCount()
                    ))
                    .toList();
        } catch (ElasticsearchException e) {
            log.error("Elasticsearch query failed: {}", e.getMessage());
            throw e;
        } catch (IOException e) {
            log.error("I/O exception during Elasticsearch request", e);
            throw new BusinessException(CODE_9105);
        }
    }
}