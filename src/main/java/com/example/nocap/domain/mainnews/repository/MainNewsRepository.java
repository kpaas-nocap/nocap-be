package com.example.nocap.domain.mainnews.repository;

import com.example.nocap.domain.mainnews.entity.MainNews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MainNewsRepository extends JpaRepository<MainNews, Long> {

}
