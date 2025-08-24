package com.example.nocap.exception;

import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(CustomException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        ErrorResponse response = ErrorResponse.builder()
            .status(errorCode.getStatus().value())
            .message(errorCode.getMessage())
            .build();
        return new ResponseEntity<>(response, errorCode.getStatus());
    }

    @ExceptionHandler(NonTransientAiException.class)
    public ResponseEntity<ErrorResponse> handleNonTransientAiException(NonTransientAiException ex) {
        // OpenAI 오류는 우리 시스템의 EXTERNAL_API_ERROR로 처리
        ErrorCode errorCode = ErrorCode.OPENAI_CREDIT_ERROR;

        ErrorResponse response = ErrorResponse.builder()
            .status(errorCode.getStatus().value())
            // 기본 메시지에 OpenAI가 보낸 구체적인 원인(ex.getMessage())을 덧붙여주면 디버깅에 용이
            .message(errorCode.getMessage() + " - " + ex.getMessage())
            .build();

        return new ResponseEntity<>(response, errorCode.getStatus());
    }
}
