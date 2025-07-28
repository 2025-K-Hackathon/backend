/// 글 작성 DTO
///PostRequestDTO.java
package com.dajeong.dajeong.dto;

import lombok.Data;

import com.dajeong.dajeong.entity.enums.Region;
import com.dajeong.dajeong.entity.enums.AgeGroup;
import com.dajeong.dajeong.entity.enums.Nationality;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class PostRequestDTO {
    @NotBlank(message = "제목을 입력하세요.")
    private String title;

    @NotBlank(message = "내용을 입력하세요.")
    private String content;

    @NotNull(message = "국적을 선택하세요.")
    private Nationality nationality;

    @NotNull(message = "지역을 선택하세요.")
    private Region region;

    @NotNull(message = "연령대를 선택하세요.")
    private AgeGroup ageGroup;
}
