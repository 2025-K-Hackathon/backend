package com.dajeong.dajeong.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class DiaryResponseDTO {
    private Long id;
    private String date;
    private String content;

    public DiaryResponseDTO(Long id, String date, String content) {
        this.id = id;
        this.date = date;
        this.content = content;
    }
}
