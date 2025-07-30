package com.example.nocap.domain.user.repository;

import com.example.nocap.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    boolean existsByUsername(String username);

    boolean existsByUserId(String userId);
    User findByUsername(String username);

    User findByUserId(String userId);
}
