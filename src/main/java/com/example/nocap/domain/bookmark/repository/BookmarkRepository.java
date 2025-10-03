package com.example.nocap.domain.bookmark.repository;

import com.example.nocap.domain.analysis.entity.Analysis;
import com.example.nocap.domain.bookmark.entity.Bookmark;
import com.example.nocap.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    Optional<Bookmark> findByUserAndAnalysis(User user, Analysis analysis);

}
