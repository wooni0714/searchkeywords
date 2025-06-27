package com.wooni.elk.common.scheduled;

import com.wooni.elk.common.keywordFilter.TrieManager;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class TrieReloadScheduler {

    private final RedisTemplate<String, String> redisTemplate;
    private final TrieManager trieManager;

    private volatile long lastLoadedTimestamp = 0L;
    private static final String LAST_MODIFIED_KEY = "badWord:lastModified";

    @PostConstruct
    public void init() {
        try {
            trieManager.reload();
            lastLoadedTimestamp = System.currentTimeMillis();
        } catch (Exception e) {
            log.error("Initial Trie load failed", e);
        }
    }


    @Scheduled(fixedDelayString = "${forbidden.pollInterval}")
    public void reloadIfChanged() {
        try {
            String tsStr = redisTemplate.opsForValue().get(LAST_MODIFIED_KEY);
            if (tsStr == null) return;

            long redisTs = Long.parseLong(tsStr);
            if (redisTs > lastLoadedTimestamp) {
                trieManager.reload();
                lastLoadedTimestamp = redisTs;
                log.info("Trie reloaded scheduler {}", redisTs);
            }
        } catch (Exception e) {
            log.error("Failed to reload Trie", e);
        }
    }
}