package com.wooni.elk.keywordPolicy.service;

import com.wooni.elk.config.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import static com.wooni.elk.keywordPolicy.dto.KeywordPolicyRequest.*;

@Service
@RequiredArgsConstructor
public class KeywordPolicyService {

    private final RedisService redisService;

    public void addBadWord(@RequestBody BadKeywordRequest badKeywordRequest) {
        redisService.addBadWord(badKeywordRequest.getBadKeyword());
    }

    public void addAllowWord (@RequestBody AllowKeywordRequest allowKeywordRequest) {
        redisService.addAllowWord(allowKeywordRequest.getAllowKeyword());
    }
}
