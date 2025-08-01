package com.dajeong.dajeong.entity;
import com.dajeong.dajeong.entity.enums.Situation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 사용자가 입력한 외국어 문장과 번역 결과를 저장
@Entity
@Table(name = "phrase")
@Getter
@NoArgsConstructor
public class Phrase {
    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Situation situation;

    // 사용자가 입력한 원문 텍스트
    @Column(length = 500, nullable = false)
    private String inputText;

    // 번역 API가 반환한 한국어 번역문
    @Column(length = 500, nullable = false)
    private String translatedText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 새 Phrase 생성 시 
    public Phrase(Situation situation,
                  String inputText,
                  String translatedText,
                  User user) {
        this.situation = situation;
        this.inputText = inputText;
        this.translatedText = translatedText;
        this.user = user;
    }

    public void setInputText(String inputText) { 
        this.inputText = inputText; 
    }
    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }
}
