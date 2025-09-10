package com.example.nocap.auth.controller;

import com.example.nocap.auth.dto.FormLoginRequest;
import com.example.nocap.auth.dto.request.FormSignupRequest;
import com.example.nocap.auth.dto.request.SignupRequest;
import com.example.nocap.auth.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

@Tag(name = "Auth", description = "로그인 관련 API")
public interface AuthSwagger {
    @Operation(
            summary = "카카오 로그인 URL 발급",
            description = "카카오 OAuth2 인증을 시작하기 위한 Authorization URL을 반환.",
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "로그인 URL 반환",
                            content = @Content( mediaType = "text/plain", schema = @Schema(implementation = String.class),
                                    examples = @ExampleObject(
                                            value = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id={REST_API_KEY}&redirect_uri={REDIRECT_URI}")))}
    )
    ResponseEntity<String> getKakaoLoginUrl();
    @Operation(
            summary = "카카오 회원가입 완료",
            description = "카카오 로그인 후 발급된 Pre-Register 토큰으로 추가 정보를 등록. 성공->최종 액세스 토큰을 Authorization 헤더로 반환",
            requestBody = @RequestBody(
                    required = true, description = "카카오 회원가입 요청 본문",
                    content = @Content(schema = @Schema(implementation = SignupRequest.class),
                            examples = @ExampleObject(
                                    value = "{\n  \"nickname\": \"mju-user\"\n}"))),
            parameters = {
                    @Parameter(
                            name = "Authorization",
                            description = "Bearer {Pre-Register Token}",
                            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "회원가입 성공 및 액세스 토큰 발급",
                            headers = { @Header(name = "Authorization", description = "Bearer {Access Token}") },
                            content = @Content(schema = @Schema(implementation = UserResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "409", description = "이미 존재하는 사용자")
            }
    )
    ResponseEntity<?> signup(SignupRequest signupRequest, String tmptoken, HttpServletResponse httpServletResponse);
    @Operation(
            summary = "카카오 로그인 처리",
            description = "카카오 Redirect URI로 로그인. 가입 이력이 있으면 액세스 토큰을, 없으면 Pre-Register 토큰을 Authorization 헤더로 반환.",
            parameters = {
                    @Parameter(name = "code", description = "Kakao Authorization Code", required = true, example = "q1w2e3r4t5y6")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "로그인 성공 또는 사전 등록 단계",
                            headers = { @Header(name = "Authorization", description = "Bearer {Access Token 또는 Pre-Register Token}") },
                            content = @Content(schema = @Schema(implementation = UserResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "잘못된 code"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            }
    )
    ResponseEntity<?> kakaoLogin(String accessCode, HttpServletResponse httpServletResponse);
    @Operation(
            summary = "일반 회원가입",
            description = "UserId, Password, Username으로 로컬 회원가입을 수행",
            requestBody = @RequestBody(
                    required = true,
                    description = "일반 회원가입 요청 본문",
                    content = @Content(schema = @Schema(implementation = FormSignupRequest.class),
                            examples = @ExampleObject(
                                    value = "{\n  \"userId\": \"user@example.com\",\n  \"password\": \"P@ssw0rd!\",\n  \"username\": \"MJU User\"\n}"))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "회원가입 성공 및 액세스 토큰 발급",
                            headers = { @Header(name = "Authorization", description = "Bearer {Access Token}") },
                            content = @Content(schema = @Schema(implementation = UserResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "409", description = "이미 존재하는 사용자")
            }
    )
    ResponseEntity<?> formSignup(FormSignupRequest req, HttpServletResponse httpServletResponse);
    @Operation(
            summary = "일반 로그인",
            description = "로컬 사용자(User ID/Password) 로그인. 성공 시 액세스 토큰을 Authorization 헤더로 반환.",
            requestBody = @RequestBody(
                    required = true,
                    description = "일반 로그인 요청 본문",
                    content = @Content(schema = @Schema(implementation = FormLoginRequest.class),
                            examples = @ExampleObject(
                                    value = "{\n  \"userId\": \"user@example.com\",\n  \"password\": \"P@ssw0rd!\"\n}"))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "로그인 성공 및 액세스 토큰 발급",
                            headers = { @Header(name = "Authorization", description = "Bearer {Access Token}") },
                            content = @Content(schema = @Schema(implementation = UserResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            }
    )
    ResponseEntity<?> formLogin(FormLoginRequest req, HttpServletResponse httpServletResponse);
    @Operation(
            summary = "로그아웃",
            description = "현재 토큰을 블랙리스트로 설정. 블랙리스트에서 60분 이후 삭제. 레디스가 아닌 메모리 형식으로 구현되어 있음",
            parameters = { @Parameter(name = "Authorization", description = "Bearer {Access Token}", required = true) },
            responses = { @ApiResponse(responseCode = "204", description = "로그아웃 완료") }
    )
    ResponseEntity<Void> logout(String authorization);

}