package com.wooni.elk.keywords.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.wooni.elk.common.keywordFilter.KeywordFilterService;
import com.wooni.elk.common.exception.BusinessException;
import com.wooni.elk.keywords.dto.SearchKeywordRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.wooni.elk.common.exception.ResultCode.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchKeywordService {
    private final KeywordFilterService keywordFilterService;

    public void logSearchKeyword(SearchKeywordRequest searchRequest) throws BusinessException {
        boolean keywordFilter = keywordFilterService.hasForbiddenWord(searchRequest.keyword());
        if (keywordFilter) {
            throw new BusinessException(CODE_9100);
        }
        log.info("event-keyword-search, {}", searchRequest.keyword());
    }
}