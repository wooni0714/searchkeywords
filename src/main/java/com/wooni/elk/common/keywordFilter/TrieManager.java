package com.wooni.elk.common.keywordFilter;

import com.wooni.elk.config.redis.RedisService;
import com.wooni.elk.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static com.wooni.elk.common.exception.ResultCode.CODE_9104;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrieManager {
    private final RedisService redisService;

    private final AtomicReference<Trie> badTrieRef = new AtomicReference<>();
    private final AtomicReference<Trie> allowTrieRef = new AtomicReference<>();
    private final Map<String, Collection<Emit>> badCache = new ConcurrentHashMap<>();
    private final Map<String, Collection<Emit>> allowCache = new ConcurrentHashMap<>();

    public synchronized void reload() throws BusinessException {
        Set<String> badWords = getBadWords();
        Set<String> allowWords = getAllowWords();

        // 현재버전 스냅샷
        Trie prevBadTrie = badTrieRef.get();
        Trie prevAllowTrie = allowTrieRef.get();

        try {
            Trie badTrie = Trie.builder().ignoreCase().addKeywords(badWords).build();
            Trie allowTrie = Trie.builder().ignoreCase().addKeywords(allowWords).build();

            badTrieRef.set(badTrie);
            allowTrieRef.set(allowTrie);

            badCache.clear();
            allowCache.clear();

            log.info("bad: {}, allow: {}", badWords.size(), allowWords.size());
        } catch (Exception e) {
            // reload 실패시 이전 버전으로 rollback
            badTrieRef.set(prevBadTrie);
            allowTrieRef.set(prevAllowTrie);

            log.error("Trie build error", e);
            throw new BusinessException(CODE_9104);
        }
    }

    public Collection<Emit> getBadEmit(String keyword) {
        return badCache.computeIfAbsent(keyword, badTrieRef.get()::parseText);
    }

    public Collection<Emit> getAllowEmit(String keyword) {
        return allowCache.computeIfAbsent(keyword, allowTrieRef.get()::parseText);
    }

    private Set<String> getBadWords() throws BusinessException {
        return redisService.getBadWords();
    }

    private Set<String> getAllowWords() throws BusinessException {
        return redisService.getAllowWords();
    }
}
