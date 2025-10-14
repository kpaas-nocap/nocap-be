package com.example.nocap.domain.mainnews.repository;

import com.example.nocap.domain.mainnews.entity.MainNews;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MainNewsRepository extends JpaRepository<MainNews, Long> {

    Optional<MainNews> findByCanonicalUrl(String canonicalUrl); // 이미 존재하는 분석 탐색용

    Optional<MainNews> findFirstByUrlOrCanonicalUrl(String url, String canonicalUrl);
}
