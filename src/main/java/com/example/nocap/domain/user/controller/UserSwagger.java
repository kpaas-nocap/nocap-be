package com.example.nocap.domain.user.controller;

import com.example.nocap.domain.user.dto.request.ChangepasswordRequest;
import com.example.nocap.domain.user.dto.request.UserUpdateRequest;
import com.example.nocap.domain.user.dto.response.UserDto;
import com.example.nocap.exception.ErrorCode;
import com.example.nocap.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
                                    examples = @ExampleObject(value = """
                {
                  "Id": 1,
                  "userId": "user@example.com",
                  "username": "MJU User",
                  "role": "ROLE_USER",
                  "userType": "KAKAO",
                  "point": 10
                }
                """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패 (토큰이 없거나 만료됨)",
                            content = @Content(
                                    schema = @Schema(implementation = ErrorCode.class),
                                    examples = @ExampleObject(value = """
                {
                  "status": 401,
                  "message": "인증이 필요한 요청입니다."
                }
                """)
                            )
                    )
            }
    )
    ResponseEntity<UserDto> getCurrentUser();

    @Operation(
            summary = "회원정보/비밀번호 수정 토큰 필요함" ,
            description = "userId, username, currentPassword, newPassword 중 제공된 필드만 반영",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UserUpdateRequest.class),
                            examples = @ExampleObject(value = "{\n  \"userId\": \"new@example.com\",\n  \"username\": \"New Name\",\n  \"currentPassword\": \"old\",\n  \"newPassword\": \"new\"\n}")
                    )
            )
    )
    ResponseEntity<UserDto> updateProfile(UserUpdateRequest request);

    @Operation(
            summary = "회원 탈퇴",
            description = "휘원 정보 삭제. 토큰만 쏘면 된 다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "탈퇴 성공"
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패",
                            content = @Content(
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(value = "{\n  \"status\": 401,\n  \"message\": \"인증이 필요한 요청입니다.\"\n}")
                            )
                    )
            }
    )
    ResponseEntity<String> deleteMe();
}
