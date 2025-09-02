package com.example.nocap.auth.kakao;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.HashMap;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoUserInfoResponseDto {

    @JsonProperty("id")
    public Long id;

    @JsonProperty("has_signed_up")
    public Boolean hasSignedUp;

    @JsonProperty("connected_at")
    public Date connectedAt;

    @JsonProperty("synched_at")
    public Date synchedAt;

    @JsonProperty("properties")
    public HashMap<String, String> properties;

    @JsonProperty("kakao_account")
    public KakaoAccount kakaoAccount;

    @JsonProperty("for_partner")
    public Partner partner;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class KakaoAccount {
        @JsonProperty("profile_needs_agreement")
        public Boolean isProfileAgree;
        @JsonProperty("profile_nickname_needs_agreement")
        public Boolean isNickNameAgree;
        @JsonProperty("profile_image_needs_agreement")
        public Boolean isProfileImageAgree;
        @JsonProperty("profile")
        public Profile profile;
        @JsonProperty("name_needs_agreement")
        public Boolean isNameAgree;
        @JsonProperty("name")
        public String name;
        @JsonProperty("email_needs_agreement")
        public Boolean isEmailAgree;
        @JsonProperty("is_email_valid")
        public Boolean isEmailValid;
        @JsonProperty("is_email_verified")
        public Boolean isEmailVerified;
        @JsonProperty("email")
        public String email;
        @JsonProperty("age_range_needs_agreement")
        public Boolean isAgeAgree;
        @JsonProperty("age_range")
        public String ageRange;
        @JsonProperty("birthyear_needs_agreement")
        public Boolean isBirthYearAgree;
        @JsonProperty("birthyear")
        public String birthYear;
        @JsonProperty("birthday_needs_agreement")
        public Boolean isBirthDayAgree;
        @JsonProperty("birthday")
        public String birthDay;
        @JsonProperty("birthday_type")
        public String birthDayType;
        @JsonProperty("gender_needs_agreement")
        public Boolean isGenderAgree;
        @JsonProperty("gender")
        public String gender;
        @JsonProperty("phone_number_needs_agreement")
        public Boolean isPhoneNumberAgree;
        @JsonProperty("phone_number")
        public String phoneNumber;
        @JsonProperty("ci_needs_agreement")
        public Boolean isCIAgree;
        @JsonProperty("ci")
        public String ci;
        @JsonProperty("ci_authenticated_at")
        public Date ciCreatedAt;

        @Getter
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public class Profile {
            @JsonProperty("nickname")
            public String nickName;
            @JsonProperty("thumbnail_image_url")
            public String thumbnailImageUrl;
            @JsonProperty("profile_image_url")
            public String profileImageUrl;
            @JsonProperty("is_default_image")
            public String isDefaultImage;
            @JsonProperty("is_default_nickname")
            public Boolean isDefaultNickName;
        }
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class Partner {
        @JsonProperty("uuid")
        public String uuid;
    }
}