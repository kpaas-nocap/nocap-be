package com.example.nocap.domain.history.entity;

import com.example.nocap.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class History {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    @ManyToOne
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    private String url;
    private String title;
    private String content;
    private String date;
    private String image;

    @CreationTimestamp // 엔티티 생성 시 자동으로 현재 시간 저장
    private LocalDateTime createdAt;
}