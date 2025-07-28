package com.dajeong.dajeong.external;

public class TranslationResult {
    private final String translatedText;
    private final String detectedSourceLang;

    public TranslationResult(String translatedText, String detectedSourceLang) {
        this.translatedText     = translatedText;
        this.detectedSourceLang = detectedSourceLang;
    }

    public String getTranslatedText() {
        return translatedText;
    }
    public String getDetectedSourceLang() {
        return detectedSourceLang;
    }
}
