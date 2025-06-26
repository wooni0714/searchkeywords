package com.wooni.elk.common.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;


@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ResultCode {

    CODE_SUCCESS(200, 200, "success"),

    CODE_9100(400, 9001, "사용할 수 없는 단어가 포함되어있습니다."),
    CODE_9101(400, 9101, "popular_keywords 의 Aggregation 결과가 존재하지 않습니다."),
    CODE_9102(400, 9102, "금칙어 목록이 Redis에 존재하지 않습니다."),
    CODE_9103(400, 9103, "허용어 목록이 Redis에 존재하지 않습니다."),
    CODE_9104(400, 9103, "금칙어/허용어 Trie 빌드 중 에러가 발생했습니다."),
    CODE_9105(400, 9103, "Elasticsearch 요청 중 오류가 발생했습니다.");

    private final Integer httpStatus;
    private final Integer code;
    private final String msg;
    private final String enMessage;

    ResultCode(Integer httpStatus, Integer code, String msg) {
        this(httpStatus, code, msg, msg);
    }

    ResultCode(Integer httpStatus, Integer code, String msg, String enMessage) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.msg = msg;
        this.enMessage = enMessage;
    }
}