package com.wooni.elk.keywordPolicy.controller;

import com.wooni.elk.common.keywordFilter.TrieManager;
import com.wooni.elk.config.redis.RedisService;
import com.wooni.elk.common.exception.BusinessException;
import com.wooni.elk.keywordPolicy.dto.KeywordPolicyRequest.AllowKeywordRequest;
import com.wooni.elk.keywordPolicy.dto.KeywordPolicyRequest.BadKeywordRequest;
import com.wooni.elk.keywordPolicy.service.KeywordPolicyService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
public class KeywordPolicyController {
    private final TrieManager trieManager;
    private final KeywordPolicyService keywordPolicyService;
    private final RedisService redisService;

    @PostMapping("/badKeyword")
    public ResponseEntity<Void> addBadKeyword(@RequestBody BadKeywordRequest badKeywordRequest) {
        keywordPolicyService.addBadWord(badKeywordRequest);
        redisService.updateLastModified();

        return ResponseEntity.ok().build();
    }

    @PostMapping("/allowKeyword")
    public ResponseEntity<Void> addAllowWords(@RequestBody AllowKeywordRequest allowKeyword) throws BusinessException {
        keywordPolicyService.addAllowWord(allowKeyword);
        trieManager.reload();

        return ResponseEntity.ok().build();
    }
}
