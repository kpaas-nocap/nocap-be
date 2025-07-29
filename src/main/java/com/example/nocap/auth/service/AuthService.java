package com.example.nocap.auth.service;

import com.example.nocap.auth.dto.SignupDto;
import com.example.nocap.domain.user.dto.UserDto;
import com.example.nocap.domain.user.entity.User;
import com.example.nocap.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDto signUp(SignupDto signupDto){
        if(userRepository.existsByUsername(signupDto.getUsername())){
            throw new IllegalArgumentException("이미 존재하는 사용자 이름입니다.");}
        else if (userRepository.existsByUserId(signupDto.getUserId())){
            throw new IllegalArgumentException("다른 이메일로 가입하세요.");}

        User user = User.from(signupDto);
        user.setUserPw(passwordEncoder.encode(user.getUserPw()));
        User savedUser = userRepository.save(user);

        return UserDto.builder()
                .Id(savedUser.getId())
                .userId(savedUser.getUserId())
                .username(user.getUsername())
                .build();
    }
}
