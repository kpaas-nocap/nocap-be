package com.example.nocap.domain.history.repository;

import com.example.nocap.domain.history.entity.History;
import com.example.nocap.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryRepository extends JpaRepository<History, Long> {

    List<History> findAllByUser(User user);

    // 특정 사용자의 히스토리 개수를 세는 메소드
    long countByUser(User user);

    // 특정 사용자의 히스토리 중 createdAt을 기준으로 오름차순 정렬하여 첫 번째 것을 찾는 메소드
    Optional<History> findFirstByUserOrderByCreatedAtAsc(User user);
}
