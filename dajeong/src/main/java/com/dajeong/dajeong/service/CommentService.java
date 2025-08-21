package com.dajeong.dajeong.service;

import com.dajeong.dajeong.dto.CommentRequestDTO;
import com.dajeong.dajeong.dto.CommentResponseDTO;
import com.dajeong.dajeong.entity.Comment;
import com.dajeong.dajeong.entity.Post;
import com.dajeong.dajeong.entity.User;
import com.dajeong.dajeong.repository.CommentRepository;
import com.dajeong.dajeong.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    // 댓글 작성
    public void addComment(Long postId, CommentRequestDTO dto, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다"));

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(dto.getContent());

        commentRepository.save(comment);
    }

    // 댓글 목록 조회
    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getComments(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다"));

        return post.getComments().stream()
                .map(comment -> {
                    User author = comment.getUser();
                    return new CommentResponseDTO(
                            comment.getId(),
                            comment.getContent(),
                            author.getName(),
                            author.getNationality().getDescription(),  // ✅ 국적
                            author.getRegion().getDescription(),       // ✅ 지역
                            comment.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());
    }

}
