///CommentController.java

package com.dajeong.dajeong.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpSession;
import java.util.List;

import com.dajeong.dajeong.dto.CommentRequestDTO;
import com.dajeong.dajeong.dto.CommentResponseDTO;
import com.dajeong.dajeong.entity.User;
import com.dajeong.dajeong.service.CommentService;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/api/posts/{postId}/comments")
    public ResponseEntity<?> write(@PathVariable Long postId,
                                   @RequestBody CommentRequestDTO dto,
                                   HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다");
        }
        commentService.addComment(postId, dto, user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/posts/{postId}/comments")
    public List<CommentResponseDTO> list(@PathVariable Long postId) {
        return commentService.getComments(postId);
    }
}
