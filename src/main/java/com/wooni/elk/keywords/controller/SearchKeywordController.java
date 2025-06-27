package com.wooni.elk.keywords.controller;

import com.wooni.elk.common.exception.BusinessException;
import com.wooni.elk.keywords.dto.PopularKeywordResponse;
import com.wooni.elk.keywords.dto.SearchKeywordRequest;
import com.wooni.elk.keywords.service.SearchKeywordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SearchKeywordController {

    private final SearchKeywordService searchKeywordService;

    @PostMapping("/search")
    public ResponseEntity<Void> searchKeyword(@RequestBody SearchKeywordRequest request) throws BusinessException {
        searchKeywordService.logSearchKeyword(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/top")
    public ResponseEntity<List<PopularKeywordResponse>> getTopKeywords() throws BusinessException {
        List<PopularKeywordResponse> topKeyword = searchKeywordService.getTopKeywordsWithin24Hours();
        return ResponseEntity.ok().body(topKeyword);
    }
}