package com.example.nocap.domain.user.service;

import com.example.nocap.domain.user.entity.User;
import com.example.nocap.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User findByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {throw new UsernameNotFoundException("User not found: " + username);}
        return user;
    }
}
