package com.example.nocap.auth.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
/* 캐시에 블랙리스트 저장 (도메인 확장이나 나중에는 Redis 권장)
    캐시 메모리에 최대 10만개의 토큰 저장, 저장된 토큰이 60분 이상 되었다면 삭제 - 토큰의 유효 시간과 일치
 */
@Component
public class TokenBlacklist {
    private final Cache<String, Boolean> cache = Caffeine.newBuilder()
            .maximumSize(100_000)
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .build();

    public void blacklist(String token, long expiresAtMillis) {
        long ttlMs = expiresAtMillis - System.currentTimeMillis();
        if (ttlMs > 0) cache.put(token, Boolean.TRUE);
    }
    public boolean isBlacklisted(String token) {
        return cache.getIfPresent(token) != null;
    }
}