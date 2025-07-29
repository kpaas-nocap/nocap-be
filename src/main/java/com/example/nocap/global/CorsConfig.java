package com.example.nocap.global;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")                     // 모든 엔드포인트에 대해
                .allowedOrigins("**")   // 허용할 Origin (예: React 개발 서버)
                .allowedMethods("*")
                .allowedHeaders("*")                       // 모든 헤더 허용
                .allowCredentials(true)                    // 쿠키 인증 허용
                .maxAge(3600);                             // pre-flight 캐시 1시간
    }
}