package com.example.nocap.domain.analysis.repository;

import com.example.nocap.domain.analysis.entity.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {

}
