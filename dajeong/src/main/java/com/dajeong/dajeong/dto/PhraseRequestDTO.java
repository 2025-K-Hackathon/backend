package com.dajeong.dajeong.dto;

public record PhraseRequestDTO(
    String inputLang,
    String inputText,
    String translatedText
) { }
