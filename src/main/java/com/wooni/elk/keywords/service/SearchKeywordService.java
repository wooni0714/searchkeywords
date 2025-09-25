package com.wooni.elk.keywords.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import com.jayway.jsonpath.JsonPath;
import com.wooni.elk.common.keywordFilter.KeywordFilterService;
import com.wooni.elk.common.exception.BusinessException;
import com.wooni.elk.keywords.dto.PopularKeywordResponse;
import com.wooni.elk.keywords.dto.SearchKeywordRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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
    private final RestClient restClient;

    public void logSearchKeyword(SearchKeywordRequest searchRequest) throws BusinessException {
        String merged = searchRequest.keyword().replace(" ", "");
        boolean keywordFilter = keywordFilterService.hasForbiddenWord(searchRequest.keyword());
        if (keywordFilter) {
            throw new BusinessException(CODE_9100);
        }

        try {
            String timestamp = OffsetDateTime.now(ZoneOffset.UTC)
                    .truncatedTo(ChronoUnit.MILLIS)
                    .format(DateTimeFormatter.ISO_INSTANT);

            List<String> tokens = extractTokensKeyword(merged);

            IndexRequest<Map<String, Object>> indexRequest = IndexRequest.of(i -> i
                    .index(INDEX_NAME)
                    .document(Map.of(
                            "searchKeyword", merged,
                            "searchTokens", tokens,
                            "searchDate", timestamp
                    ))
            );

            client.index(indexRequest);
        } catch (IOException e) {
            log.error("extractTokensKeyword{}", e.getMessage());
            throw new BusinessException(CODE_9100);
        }
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
                                    .field("searchTokens")
                                    .size(size)
                            )
                    )
            );

            SearchResponse<Void> resp = client.search(req, Void.class);

            Map<String, Aggregate> ages = resp.aggregations();
            Aggregate agg = ages.get("popular_keywords");
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
            throw new BusinessException(CODE_9107);
        } catch (IOException e) {
            log.error("I/O exception during Elasticsearch request", e);
            throw new BusinessException(CODE_9105);
        }
    }

    private List<String> extractTokensKeyword(String keyword) throws IOException {
        var request = new Request("POST", "/searchindexkeyword/_analyze");
        request.setJsonEntity("""
                {
                    "analyzer": "nori_analyzer",
                    "text": "%s"
                }
                """.formatted(keyword));

        log.info("request: {}", request);
        var response = restClient.performRequest(request);
        log.info("response: {}", response);
        var jsonData = EntityUtils.toString(response.getEntity());
        log.info("jsonData: {}", jsonData);

        return JsonPath.read(jsonData, "$.tokens[*].token");
    }
}