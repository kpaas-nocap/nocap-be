package com.example.nocap.domain.user.controller;

import com.example.nocap.domain.user.dto.UserDto;
import com.example.nocap.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "User", description = "사용자 정보 API")
public interface UserSwagger {
    @Operation(
            summary = "현재 로그인된 사용자 조회",
            description = "현재 로그인한 사용자의 정보를 반환.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(
                                    schema = @Schema(implementation = UserDto.class),
                                    examples = @ExampleObject(value = "{\n  \"id\": 1,\n  \"userId\": \"user@example.com\",\n  \"username\": \"MJU User\",\n  \"role\": \"ROLE_USER\"\n}"))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패 (토큰이 없거나 만료됨)",
                            content = @Content(
                                    schema = @Schema(implementation = ErrorCode.class),
                                    examples = @ExampleObject(value = "{\n  \"code\": \"UNAUTHORIZED\",\n  \"message\": \"로그인이 필요합니다.\"\n}")))
            }
    )
    ResponseEntity<UserDto> getCurrentUser();
}