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
    public List<CommentResponseDTO> getComments(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId).stream()
                .map(c -> new CommentResponseDTO(
                        c.getId(),
                        c.getContent(),
                        c.getUser().getName(),                     // 작성자 이름
                        c.getUser().getNationality().getDescription(), // 작성자 국적
                        c.getUser().getRegion().getDescription(),      // 작성자 지역
                        c.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }
}
