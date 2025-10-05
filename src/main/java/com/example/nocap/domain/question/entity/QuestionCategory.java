package com.example.nocap.domain.question.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QuestionCategory {
    INQUIRY("이용문의"),
    ACCOUNT("계정·회원"),
    PAYMENT("결제·환불"),
    BUG("오류·버그"),
    RESTRICTION("기능제안"),
    SECURITY("보안·신고"),
    ETC("기타문의");

    private final String title;
}
