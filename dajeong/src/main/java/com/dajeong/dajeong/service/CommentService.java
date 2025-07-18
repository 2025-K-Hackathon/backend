///CommentService.java

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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    // 댓글 작성: User 파라미터 추가
    public void addComment(Long postId, CommentRequestDTO dto, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다"));

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(dto.getContent());

        commentRepository.save(comment);
    }

    // 댓글 목록 조회: authorName 매핑 추가
    public List<CommentResponseDTO> getComments(Long postId) {
        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        return comments.stream()
                .map(c -> new CommentResponseDTO(
                        c.getId(),
                        c.getContent(),
                        c.getUser().getName(),   // 작성자 이름
                        c.getCreatedAt()))
                .collect(Collectors.toList());
    }
}
