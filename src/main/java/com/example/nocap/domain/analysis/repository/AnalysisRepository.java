package com.example.nocap.domain.analysis.repository;

import com.example.nocap.domain.analysis.entity.Analysis;
import com.example.nocap.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {

    List<Analysis> findAllByCategory(String category);

    List<Analysis> findAllByUserId(Long id);
}
