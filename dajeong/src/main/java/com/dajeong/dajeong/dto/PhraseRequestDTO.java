package com.dajeong.dajeong.dto;

public class PhraseRequestDTO {
    private String inputText; // 사용자가 입력하는 원문

    public PhraseRequestDTO() {}
    public String getInputText() { 
        return inputText; 
    }
    public void setInputText(String inputText) {
        this.inputText = inputText;
    }
}