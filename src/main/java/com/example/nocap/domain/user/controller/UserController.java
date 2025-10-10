package com.example.nocap.domain.user.controller;

import com.example.nocap.domain.user.dto.request.UserUpdateRequest;
import com.example.nocap.domain.user.dto.response.UserDto;
import com.example.nocap.domain.user.dto.request.ChangepasswordRequest;
import com.example.nocap.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/nocap/user")
public class UserController implements UserSwagger{
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        return ResponseEntity.ok(userService.getMe());
    }

    @PatchMapping("/change/password")
    public ResponseEntity<String> changePassword(@RequestBody ChangepasswordRequest request){
        userService.changePassword(request);
        return ResponseEntity.ok("비밀번호 변경이 완료되었습니다.");
    }
    @PatchMapping("/update")
    public ResponseEntity<UserDto> updateProfile(@RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateProfile(request));
    }

    @DeleteMapping
    public ResponseEntity<String> deleteMe(){
        userService.deleteMe();
        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
    }
}
