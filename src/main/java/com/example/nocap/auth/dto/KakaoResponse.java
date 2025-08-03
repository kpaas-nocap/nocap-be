package com.example.nocap.auth.dto;

import java.util.Map;

public class KakaoResponse implements OAuth2Response {
    private final Map<String,Object> attrs;

    @SuppressWarnings("unchecked")
    public KakaoResponse(Map<String,Object> attributes) {
        this.attrs = attributes;
    }

    @Override public String getProvider()     { return "kakao"; }
    @Override public String getProviderId()   { return attrs.get("id").toString(); }

    @SuppressWarnings("unchecked")
    @Override public String getEmail() {
        Map<String,Object> account = (Map<String,Object>) attrs.get("kakao_account");
        return account.get("email").toString();
    }

    @SuppressWarnings("unchecked")
    @Override public String getName() {
        Map<String,Object> props = (Map<String,Object>) attrs.get("properties");
        return props.get("nickname").toString();
    }
}