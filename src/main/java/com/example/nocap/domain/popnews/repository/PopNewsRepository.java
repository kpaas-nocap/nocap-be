package com.example.nocap.domain.popnews.repository;

import com.example.nocap.domain.popnews.entity.PopNews;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopNewsRepository extends JpaRepository<PopNews, Long> {

}
