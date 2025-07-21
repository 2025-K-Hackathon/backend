/// entity/Diary.java

package com.dajeong.dajeong.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

    @Column(columnDefinition = "TEXT")
    private String correctedText;

    @Column(columnDefinition = "TEXT")
    private String reply;

    @ElementCollection
    @CollectionTable(name = "diary_incorrect_words", joinColumns = @JoinColumn(name = "diary_id"))
    @Column(name = "word")
    private List<String> incorrectWords;

    @ElementCollection
    @CollectionTable(name = "diary_corrected_words", joinColumns = @JoinColumn(name = "diary_id"))
    @Column(name = "word")
    private List<String> correctedWords;

    @Column(columnDefinition = "json")
    private String correctionsJson;

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
