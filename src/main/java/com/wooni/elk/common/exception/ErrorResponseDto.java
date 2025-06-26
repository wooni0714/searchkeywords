package com.wooni.elk.common.exception;


public record ErrorResponseDto(
        int code,
        String message
) {
    public static ErrorResponseDto of(final ResultCode resultCode) {
        return new ErrorResponseDto(resultCode.getCode(), resultCode.getEnMessage());
    }
}
