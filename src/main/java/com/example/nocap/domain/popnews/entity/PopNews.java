package com.example.nocap.domain.popnews.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PopNews {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long popNewsId;

    private String url;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String image;

    private String date;


}
