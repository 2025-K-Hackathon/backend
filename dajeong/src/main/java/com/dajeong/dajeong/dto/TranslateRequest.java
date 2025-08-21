package com.dajeong.dajeong.dto;

public class TranslateRequest {
    private String text; // 번역할 원문
    private String source; // 원문 언어코드 
    private String target; // 목표 언어코드 

    public TranslateRequest() {}

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
    }
    public String getTarget() {
        return target;
    }
    public void setTarget(String target) {
        this.target = target;
    }
}
