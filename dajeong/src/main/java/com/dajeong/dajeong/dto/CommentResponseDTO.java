///CommentResponseDTO.java

package com.dajeong.dajeong.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CommentResponseDTO {
    private Long id;
    private String content;
    private String authorName;
    private LocalDateTime createdAt;
}
