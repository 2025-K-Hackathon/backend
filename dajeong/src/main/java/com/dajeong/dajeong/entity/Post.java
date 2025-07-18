///커뮤니티 글 Entity
///Post.java
package com.dajeong.dajeong.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

import com.dajeong.dajeong.entity.Comment;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 댓글 연관관계: Post 삭제 시 연관된 Comment들도 함께 삭제
    @OneToMany(
            mappedBy = "post",
            cascade = CascadeType.REMOVE,
            orphanRemoval = true,
            fetch = FetchType.LAZY      // 기본 LAZY지만 명시하는 것이 좋습니다
    )
    private List<Comment> comments = new ArrayList<>();

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String nationality;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private String ageGroup;

    @Column(nullable = false)
    private int likeCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}