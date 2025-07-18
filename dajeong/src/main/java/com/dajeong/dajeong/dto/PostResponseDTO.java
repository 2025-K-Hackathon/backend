///글 응답 DTO (작성자, 좋아요 수 포함)
///PostResponseDTO.java

package com.dajeong.dajeong.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PostResponseDTO {
    private Long id;
    private String title;
    private String content;
    private String authorName;
    private String nationality;
    private String region;
    private String ageGroup;
    private int likeCount;
    private LocalDateTime createdAt;
}
