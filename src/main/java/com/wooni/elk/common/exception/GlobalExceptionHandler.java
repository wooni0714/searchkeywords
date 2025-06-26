package com.wooni.elk.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDto> handleCustomException(final BusinessException e) {
        ResultCode resultCode = e.getCode();
        log.error("[BusinessException] code: {}, message: {}", resultCode.getCode(), e.getMessage(), e);

        ErrorResponseDto errorResponse = ErrorResponseDto.of(resultCode);
        return ResponseEntity.status(resultCode.getHttpStatus()).body(errorResponse);
    }
}