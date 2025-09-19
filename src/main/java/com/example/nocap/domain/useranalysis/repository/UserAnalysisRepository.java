package com.example.nocap.domain.useranalysis.repository;

import com.example.nocap.domain.useranalysis.entity.UserAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAnalysisRepository extends JpaRepository<UserAnalysis, Long> {
}
