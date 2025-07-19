/// entity/Diary.java

package com.dajeong.dajeong.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Diary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDate date;

    @Column(columnDefinition = "TEXT")
    private String content;

    // 시각은 자동 저장
    private LocalDateTime createdAt = LocalDateTime.now();
    
    public static Diary of(User user, LocalDate date, String content) {
        Diary d = new Diary();
        d.setUser(user);
        d.setDate(date);
        d.setContent(content);
        return d;
    }
}
