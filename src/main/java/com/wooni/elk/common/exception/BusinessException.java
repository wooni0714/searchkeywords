package com.wooni.elk.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends Exception{
    private final ResultCode code;
    public BusinessException(ResultCode code) {
        super(code.getEnMessage());
        this.code = code;
    }
}