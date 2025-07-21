// DiaryAIModelResultDTO.java

package com.dajeong.dajeong.dto;

import lombok.Data;


@Data
public class DiaryAIModelResultDTO {
    private String originalText;        // 원본
    private String fullCorrectedText;   // 교정본
    private String reply;               // 공감 답글
}