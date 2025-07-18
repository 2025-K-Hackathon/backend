///CommentRequestDTO.java

package com.dajeong.dajeong.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class CommentRequestDTO {
    @NotBlank(message = "댓글 내용을 입력하세요.")
    private String content;
}
