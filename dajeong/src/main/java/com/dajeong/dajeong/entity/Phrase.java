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

    // 사용자가 입력한 원문 텍스트
    @Column(length = 500, nullable = false)
    private String inputText;

    // 번역 API가 반환한 한국어 번역문
    @Column(length = 500, nullable = false)
    private String translatedText;

    //private String ttsUrl;

    protected Phrase() { }  

    // 새 Phrase 생성 시 
    public Phrase(Situation situation,
                  String inputText,
                  String translatedText) {
        this.situation      = situation;
        this.inputText      = inputText;
        this.translatedText = translatedText;
    }
    public Long getId() { return id; }
    public Situation getSituation() { return situation; }
    public String getInputText() { return inputText; }
    public String getTranslatedText() { return translatedText; }

    public void setInputText(String inputText) { 
        this.inputText = inputText; 
    }
    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }
    //public String getTtsUrl() { return ttsUrl; }
    //public void setTtsUrl(String ttsUrl) { this.ttsUrl = ttsUrl; }
}
