package com.example.nocap.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400 Bad Request: 잘못된 요청
    NOT_A_NEWS_ARTICLE(HttpStatus.BAD_REQUEST, "제공된 URL은 뉴스 기사가 아닙니다."),

    // 401 Unauthorized: 인증되지 않은 사용자
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요한 요청입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),

    // 404 Not Found: 리소스를 찾을 수 없음
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다."),
    ARTICLE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 기사를 찾을 수 없습니다."),
    ANALYSIS_NOT_FOUND(HttpStatus.NOT_FOUND, "분석을 찾을 수 없습니다."),
    HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "검색기록을 찾을 수 없습니다."),

    // 409 Conflict: 충돌
    DUPLICATE_MEMBER_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),

    // 500 Internal Server Error: 서버 내부 오류
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부에 알 수 없는 오류가 발생했습니다."),
    EXTERNAL_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "외부 API 호출 중 오류가 발생했습니다."),
    LAMBDA_REQUEST_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "람다 호출 중 오류가 발생했습니다."),
    OPENAI_CREDIT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "OpenAI의 크레딧이 부족하여 오류가 발생했습니다.");



    private final HttpStatus status;       // HTTP 상태 코드
    private final String message;        // 에러 메시지
}