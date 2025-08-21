package com.dajeong.dajeong.dto;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.dajeong.dajeong.entity.Phrase;
import com.dajeong.dajeong.entity.enums.Situation;

public record PhraseResponseDTO(
    Long id,
    String situation,
    String inputText,
    String translatedText,
    String iconUrl,
    String ttsUrl
)
{
    public static PhraseResponseDTO fromEntity(Phrase p, String ttsUrl) {
        Situation s = p.getSituation();
        String iconUrl = ServletUriComponentsBuilder
            .fromCurrentContextPath() 
            .path("/images/icons/")
            .path(s.getIconFile())
            .toUriString();
        return new PhraseResponseDTO(
            p.getId(),
            p.getSituation().name(),
            p.getInputText(),
            p.getTranslatedText(),
            iconUrl,
            ttsUrl

        );
    }
    public static PhraseResponseDTO fromEntity(Phrase p) {
        return fromEntity(p, null);   // or "" 빈 문자열로 해도 OK
    }
}
