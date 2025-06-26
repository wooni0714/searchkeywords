package com.wooni.elk.keywordPolicy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class KeywordPolicyRequest {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BadKeywordRequest {
        private List<String> badKeyword;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AllowKeywordRequest {
        private List<String> allowKeyword;
    }
}
