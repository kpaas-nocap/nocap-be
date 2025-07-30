package com.example.nocap.domain.user.controller;

import com.example.nocap.domain.user.dto.UserDto;
import com.example.nocap.domain.user.entity.User;
import com.example.nocap.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/nocap/user")
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(Authentication authentication)
    {
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        UserDto dto = UserDto.builder()
                .Id(user.getId())
                .userId(user.getUserId())
                .username(user.getUsername())
                .build();
        return ResponseEntity.ok(dto);
    }
}
