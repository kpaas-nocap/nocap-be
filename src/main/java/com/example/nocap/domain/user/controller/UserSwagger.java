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
                                    examples = @ExampleObject(value = "{\n  \"id\": 1,\n  \"userId\": \"user@example.com\",\n  \"username\": \"MJU User\",\n  \"role\": \"ROLE_USER\"\n}")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패 (토큰이 없거나 만료됨)",
                            content = @Content(
                                    schema = @Schema(implementation = ErrorCode.class),
                                    examples = @ExampleObject(value = "{\n  \"status\": 401,\n  \"message\": \"인증이 필요한 요청입니다.\"\n}")
                            )
                    )
            }
    )
    ResponseEntity<UserDto> getCurrentUser();

    @Operation(
            summary = "비밀번호 변경",
            description = "현재 로그인된 사용자의 비밀번호 변경. 카카오 로그인 사용자는 변경 X" +
                    "토큰 필요~",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "비밀번호 변경 성공",
                            content = @Content(
                                    examples = @ExampleObject(value = "비밀번호가 성공적으로 변경되었습니다.")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "현재 비밀번호가 올바르지 않음",
                            content = @Content(
                                    schema = @Schema(implementation = ErrorCode.class),
                                    examples = @ExampleObject(value = "{\n  \"status\": 400,\n  \"message\": \"현재 비밀번호가 올바르지 않습니다.\"\n}")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "비밀번호 변경 권한 없음 (FORM 사용자가 아님)",
                            content = @Content(
                                    schema = @Schema(implementation = ErrorCode.class),
                                    examples = @ExampleObject(value = "{\n  \"status\": 403,\n  \"message\": \"비밀번호 변경은 FORM 사용자만 가능합니다.\"\n}")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증되지 않은 사용자 (로그인 필요)",
                            content = @Content(
                                    schema = @Schema(implementation = ErrorCode.class),
                                    examples = @ExampleObject(value = "{\n  \"status\": 401,\n  \"message\": \"인증이 필요한 요청입니다.\"\n}")
                            )
                    )
            }
    )
    ResponseEntity<String> changePassword(ChangepasswordRequest request);
    @Operation(
            summary = "회원정보 수정 (비밀번호 제외)",
            description = "현재 로그인 사용자의 userId, username을 수정. 토큰 필요~",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UserUpdateRequest.class),
                            examples = @ExampleObject(value = "{\n  \"userId\": \"new@example.com\",\n  \"username\": \"New Name\"\n}")
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "수정 성공",
                            content = @Content(schema = @Schema(implementation = UserDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 필요",
                            content = @Content(schema = @Schema(implementation = ErrorCode.class))
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "중복된 userId 또는 username",
                            content = @Content(schema = @Schema(implementation = ErrorCode.class))
                    )
            }
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
