package com.example.nocap.domain.analysis.repository;

import com.example.nocap.domain.analysis.entity.Analysis;
import com.example.nocap.domain.mainnews.entity.MainNews;
import com.example.nocap.domain.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {

    List<Analysis> findAllByCategory(String category);

    @Query("SELECT a FROM Analysis a JOIN a.userAnalyses ua WHERE ua.user.id = :userId")
    List<Analysis> findAllByUserId(@Param("userId") Long userId);

    Optional<Analysis> findByMainNews(MainNews mainNews);

    void deleteByDateBefore(LocalDateTime threshold);
}
