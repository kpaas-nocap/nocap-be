package com.example.nocap.domain.question.entity;

import com.example.nocap.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionId;

    // 질문 작성자 (User와 N:1 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING) // DB에 Enum 이름을 문자열로 저장
    @Column(nullable = false)
    private QuestionCategory category;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(columnDefinition = "TEXT")
    private String answer;
    private LocalDateTime answeredAt;

    public void setUser(User user) {
        this.user = user;
        if (user != null && !user.getQuestions().contains(this)) {
            user.getQuestions().add(this);
        }
    }

}
