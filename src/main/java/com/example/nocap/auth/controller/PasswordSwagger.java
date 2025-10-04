package com.example.nocap.auth.controller;

import com.example.nocap.auth.dto.request.IssueTempPasswordRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Password", description = "비밀번호 관련 API")
public interface PasswordSwagger {

    @Operation(
            summary = "임시 비밀번호 발급",
            description = """
                    이메일(User ID)을 입력하면 임시 비밀번호를 해당 메일로 발송합니다. 
                    <br>⚠️ 헤더 인증 필요 없음.
                    """,
            requestBody = @RequestBody(
                    required = true,
                    description = "임시 비밀번호 발급 요청 본문",
                    content = @Content(
                            schema = @Schema(implementation = IssueTempPasswordRequest.class),
                            examples = @ExampleObject(
                                    value = "{\n  \"userId\": \"user@example.com\"\n}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "임시 비밀번호 발급 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 존재하지 않는 사용자"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류 (메일 전송 실패 등)")
            }
    )
    ResponseEntity<Void> issueTemp(IssueTempPasswordRequest req);
}