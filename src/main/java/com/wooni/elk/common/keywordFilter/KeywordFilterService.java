package com.wooni.elk.common.keywordFilter;

import lombok.RequiredArgsConstructor;
import org.ahocorasick.trie.Emit;
import org.springframework.stereotype.Service;

import java.util.Collection;


@Service
@RequiredArgsConstructor
public class KeywordFilterService {

    private final TrieManager trieManager;

    public boolean hasForbiddenWord(String keyword) {
        Collection<Emit> badMatches = trieManager.getBadEmit(keyword);
        Collection<Emit> allowMatches = trieManager.getAllowEmit(keyword);

        for (Emit badEmit : badMatches) {
            boolean isContained = allowMatches.stream().anyMatch(allow ->
                    allow.getStart() <= badEmit.getStart() && allow.getEnd() >= badEmit.getEnd()
            );
            if (!isContained) return true;
        }

        return false;
    }
}