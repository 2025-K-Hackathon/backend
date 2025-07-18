/// 글 작성 DTO
///PostRequestDTO.java
package com.dajeong.dajeong.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class PostRequestDTO {
    @NotBlank(message = "제목을 입력하세요.")
    private String title;

    @NotBlank(message = "내용을 입력하세요.")
    private String content;

    @NotBlank(message = "국적을 선택하세요.")
    private String nationality;

    @NotBlank(message = "지역을 선택하세요.")
    private String region;

    @NotBlank(message = "연령대를 선택하세요.")
    private String ageGroup;
}
