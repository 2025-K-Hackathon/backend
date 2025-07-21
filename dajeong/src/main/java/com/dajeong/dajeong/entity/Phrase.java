package com.dajeong.dajeong.entity;
import com.dajeong.dajeong.entity.enums.Situation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// 사용자가 입력한 외국어 문장과 번역 결과를 저장
@Entity
@Table(name = "phrase")
public class Phrase {
    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Situation situation;

    // 자동 감지된 원문 언어 코드
    @Column(length = 5, nullable = false)
    private String inputLang;

    // 사용자가 입력한 원문 텍스트
    @Column(length = 500, nullable = false)
    private String inputText;

    // 번역 API가 반환한 한국어 번역문
    @Column(length = 500, nullable = false)
    private String translatedText;

    protected Phrase() { }  

    // 새 Phrase 생성 시 
    public Phrase(Situation situation,
                  String inputLang,
                  String inputText,
                  String translatedText) {
        this.situation      = situation;
        this.inputLang      = inputLang;
        this.inputText      = inputText;
        this.translatedText = translatedText;
    }
    public Long getId() { return id; }
    public Situation getSituation() { return situation; }
    public String getInputLang() { return inputLang; }
    public String getInputText() { return inputText; }
    public String getTranslatedText() { return translatedText; }

    public void setInputText(String inputText) { this.inputText = inputText; }
}
