package com.dajeong.dajeong.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class DiaryRequestDTO {
    // yyyy-MM-dd 형태의 날짜 문자열
    private String date;
    private String content;
}
