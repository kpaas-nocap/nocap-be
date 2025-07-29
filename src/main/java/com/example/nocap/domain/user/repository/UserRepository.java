package com.example.nocap.domain.user.repository;

import com.example.nocap.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
    boolean existsByUsername(String username);

    boolean existsByUserId(String userId);
}
