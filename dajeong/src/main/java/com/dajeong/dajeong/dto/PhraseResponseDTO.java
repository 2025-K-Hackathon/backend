package com.dajeong.dajeong.dto;
import com.dajeong.dajeong.entity.Phrase;

public record PhraseResponseDTO(
    Long id,
    String situation,
    String inputLang,
    String inputText,
    String translatedText
)
{
    public static PhraseResponseDTO fromEntity(Phrase p) {
        return new PhraseResponseDTO(
            p.getId(),
            p.getSituation().name(),
            p.getInputLang(),
            p.getInputText(),
            p.getTranslatedText()
        );
    }
}
