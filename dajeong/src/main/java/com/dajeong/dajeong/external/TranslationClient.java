package com.dajeong.dajeong.external;

public interface TranslationClient {
    /**
     * @param text 원문 텍스트
     * @return 번역 결과(번역된 텍스트 + 감지된 언어코드)
     */
    TranslationResult translateToKorean(String text);
}
