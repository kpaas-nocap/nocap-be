package com.example.nocap.auth.kakao;


import com.example.nocap.exception.CustomException;
import com.example.nocap.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class KakaoUtil {

    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private final String KAUTH_TOKEN_URL_HOST;
    private final String KAUTH_USER_URL_HOST;

    @Autowired
    public KakaoUtil(@Value("${spring.kakao.auth.client}") String clientId,
                     @Value("${spring.kakao.auth.client-secret}") String clientSecret,
                     @Value("${spring.kakao.auth.redirect}") String redirectUri) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        KAUTH_TOKEN_URL_HOST = "https://kauth.kakao.com";
        KAUTH_USER_URL_HOST = "https://kapi.kakao.com";
    }

    public String getAccessTokenFromKakao(String code) {
        KakaoTokenResponseDTO kakaoTokenResponseDto = WebClient.create(KAUTH_TOKEN_URL_HOST).post()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .path("/oauth/token")
                        .queryParam("grant_type", "authorization_code")
                        .queryParam("client_id", clientId)
                        .queryParam("client_secret", clientSecret)
                        .queryParam("redirect_uri", redirectUri)
                        .queryParam("code", code)
                        .build(true))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .retrieve()
                .onStatus(HttpStatusCode::isError, r ->
                        r.bodyToMono(String.class).map(body -> {
                            log.error("카카오 토큰 발급 실패: {}", body);   // 🔴 에러 응답 그대로 찍음
                            return new CustomException(ErrorCode.EXTERNAL_API_ERROR);
                        })
                )
                .bodyToMono(KakaoTokenResponseDTO.class)
                .block();
        if (kakaoTokenResponseDto == null || kakaoTokenResponseDto.getAccessToken() == null) {
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
        }
        log.info("accessToken={}", kakaoTokenResponseDto.getAccessToken());
        return kakaoTokenResponseDto.getAccessToken();
    }

    public KakaoUserInfoResponseDto getUserInfo(String accessToken) {
        KakaoUserInfoResponseDto userInfo = WebClient.create(KAUTH_USER_URL_HOST)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .path("/v2/user/me")
                        .build(true))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r -> Mono.error(new CustomException(ErrorCode.EXTERNAL_API_ERROR)))
                .onStatus(HttpStatusCode::is5xxServerError, r -> Mono.error(new CustomException(ErrorCode.EXTERNAL_API_ERROR)))
                .bodyToMono(KakaoUserInfoResponseDto.class)
                .block();
        if (userInfo == null) throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
        return userInfo;
    }
}