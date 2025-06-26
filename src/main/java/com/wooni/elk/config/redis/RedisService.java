package com.wooni.elk.config.redis;

import com.wooni.elk.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static com.wooni.elk.common.Const.*;
import static com.wooni.elk.common.exception.ResultCode.*;

@Component
@RequiredArgsConstructor
public class RedisService {
    private static final Logger log = LogManager.getLogger(RedisService.class);
    private final RedisTemplate<String, String> redisTemplate;

    public Set<String> getBadWords() throws BusinessException {
        try {
            return redisTemplate.opsForSet().members(BAD_KEYWORD_PREFIX);
        } catch (Exception e) {
            log.error("No bad words found in Redis: {}", e.getMessage(), e);
            throw new BusinessException(CODE_9102);
        }
    }

    public Set<String> getAllowWords() throws BusinessException {
        try {
            return redisTemplate.opsForSet().members(ALLOW_KEYWORD_PREFIX);
        } catch (Exception e) {
            log.error("No allow words found in Redis: {}", e.getMessage(), e);
            throw new BusinessException(CODE_9103);
        }
    }


    public void addBadWord(List<String> badWords) {
        for (String badWord : badWords) {
            redisTemplate.opsForSet().add(BAD_KEYWORD_PREFIX, badWord);
        }
    }

    public void addAllowWord(List<String> allowWords) {
        for (String allowWord : allowWords) {
            redisTemplate.opsForSet().add(ALLOW_KEYWORD_PREFIX, allowWord);
        }
    }

    public void updateLastModified() {
        String now = String.valueOf(System.currentTimeMillis());
        redisTemplate.opsForValue().set(LAST_MODIFIED_KEY, now);
    }
}