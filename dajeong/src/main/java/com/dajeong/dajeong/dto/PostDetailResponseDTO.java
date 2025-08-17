// dto/PostDetailResponseDTO.java
package com.dajeong.dajeong.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class PostDetailResponseDTO {
    private Long id;
    private String title;
    private String content;
    private String authorName;
    private String nationality;
    private String region;
    private String ageGroup;
    private int likeCount;
    private Long authorId;
    private int commentCount;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
    private boolean likedByCurrentUser;
}
