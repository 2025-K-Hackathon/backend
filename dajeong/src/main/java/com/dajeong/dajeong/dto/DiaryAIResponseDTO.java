// DiaryAIResponseDTO.java
package com.dajeong.dajeong.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class DiaryAIResponseDTO {
    private String originalText;
    private String fullCorrectedText;
    private String reply;
    private List<Correction> corrections;

    @Getter @Setter
    public static class Correction {
        private String incorrect;   // 틀린 단어
        private String corrected;   // 수정 단어
        private int start;          // 시작 인덱스
        private int end;            // 끝 인덱스
    }
}
