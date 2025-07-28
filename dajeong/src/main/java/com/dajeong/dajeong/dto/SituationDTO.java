package com.dajeong.dajeong.dto;

import com.dajeong.dajeong.entity.enums.Situation;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SituationDTO(
    String type,
    String displayName,
    String iconUrl,
    String ttsUrl
) {
    public static SituationDTO basicOf(Situation s) {
        String iconBase = ServletUriComponentsBuilder
            .fromCurrentContextPath()    // → https://your-domain.com
            .path("/images/icons/")      // → + "/images/icons/"
            .toUriString();

        return new SituationDTO(
            s.name(),
            s.getDisplayName(),
            iconBase + s.getIconFile(),  // 절대경로 아이콘 URL
            null
        );
    }
    public static SituationDTO of(com.dajeong.dajeong.entity.enums.Situation s) {
        String ttsBase = ServletUriComponentsBuilder
            .fromCurrentContextPath()
            .path("/api/tts/synthesize/situation/")
            .path(s.name())
            .toUriString();

        String iconBase = ServletUriComponentsBuilder
            .fromCurrentContextPath()
            .path("/images/icons/")
            .toUriString();

        return new SituationDTO(
            s.name(),
            s.getDisplayName(),
            iconBase + s.getIconFile(),
            ttsBase
        );
    }
}