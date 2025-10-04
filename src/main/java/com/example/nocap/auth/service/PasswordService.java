package com.example.nocap.auth.service;

import com.example.nocap.auth.service.mail.MailService;
import com.example.nocap.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordService {
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final MailService mailService;

    public void issueTempPasswordByUserId(String userId){
        userRepository.findByUserId(userId).ifPresent(u->{
            String temp= UUID.randomUUID().toString().replace("-","").substring(0,12);
            u.setUserPw(encoder.encode(temp));
            userRepository.save(u);
            mailService.send(u.getUserId(),"임시 비밀번호.","회원님의 임시 비밀번호는 다음과 같습니다. : "+temp);
            System.out.println("issued temp => " + temp);
        });
    }
}