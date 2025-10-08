package com.example.nocap.domain.user.service;

import com.example.nocap.auth.support.AuthContext;
import com.example.nocap.domain.user.dto.response.UserDto;
import com.example.nocap.domain.user.dto.request.ChangepasswordRequest;
import com.example.nocap.domain.user.entity.User;
import com.example.nocap.domain.user.repository.UserRepository;
import com.example.nocap.exception.CustomException;
import com.example.nocap.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User findByUsername(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {throw new UsernameNotFoundException("User not found: " + username);}
        return user;
    }

    public User findByUserId(String userId) {
        return userRepository.findByUserId(userId).orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));
    }

    public UserDto getMe() {
        Long id = AuthContext.currentUserPk();
        if (id == null) throw new CustomException(ErrorCode.UNAUTHORIZED);
        User user = userRepository.findById(id).orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));
        return UserDto.builder()
                .Id(user.getId())
                .userId(user.getUserId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    public void changePassword(ChangepasswordRequest req){
        Long id = AuthContext.currentUserPk();
        if(id == null)throw new CustomException(ErrorCode.UNAUTHORIZED);
        User user = userRepository.findById(id).orElseThrow(()->new CustomException(ErrorCode.UNAUTHORIZED));
        if(!"FORM".equalsIgnoreCase(user.getUserType()))throw new CustomException(ErrorCode.FORBIDDEN_PASSWORD_CHANGE);
        if(!passwordEncoder.matches(req.getCurrentPassword(),user.getUserPw()))throw new CustomException(ErrorCode.INVALID_CURRENT_PASSWORD);
        user.setUserPw(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }
}
