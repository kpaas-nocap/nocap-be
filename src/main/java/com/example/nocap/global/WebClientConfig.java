package com.example.nocap.global;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${fastapi.server.url}")
    private String fastapiUrl;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
            .baseUrl(fastapiUrl) // 모든 요청의 기본 URL을 FastAPI 서버 주소로 설정
            .build();
    }
}