package com.example.nocap.auth.service;

import com.example.nocap.auth.dto.CustomOAuth2User;
import com.example.nocap.auth.dto.KakaoResponse;
import com.example.nocap.auth.dto.OAuth2Response;
import com.example.nocap.auth.dto.UserDto;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    @Override
    public OAuth2User loadUser(OAuth2UserRequest req) throws OAuth2AuthenticationException {
        OAuth2User oUser = super.loadUser(req);
        String regId = req.getClientRegistration().getRegistrationId();

        OAuth2Response resp;

        if ("kakao".equals(regId))  resp = new KakaoResponse(oUser.getAttributes());
        else throw new OAuth2AuthenticationException("Unsupported provider: " + regId);

        String username = resp.getProvider() + " " + resp.getProviderId();
        UserDto dto = new UserDto();
        dto.setUserId(username);
        dto.setUsername(resp.getName());
        dto.setRole("ROLE_USER");

        return new CustomOAuth2User(dto);
    }
}