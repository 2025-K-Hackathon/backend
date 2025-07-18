///글 작성, 목록 조회, 상세 조회, 삭제
/// PostController.java
package com.dajeong.dajeong.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpSession;
import java.util.List;

import com.dajeong.dajeong.dto.PostRequestDTO;
import com.dajeong.dajeong.dto.PostResponseDTO;
import com.dajeong.dajeong.entity.User;
import com.dajeong.dajeong.service.PostService;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody PostRequestDTO dto, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body("로그인 필요");
        }
        postService.createPost(dto, user);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public List<PostResponseDTO> getPosts(
            @RequestParam(required = false) String nationality,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String ageGroup,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "recent") String sort
    ) {
        return postService.getFilteredPosts(nationality, region, ageGroup, keyword, sort);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다");
        }
        boolean success = postService.deletePost(id, user);
        if (success) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(403).body("삭제 권한이 없습니다");
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<?> likePost(@PathVariable Long postId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다");
        }

        boolean liked = postService.likePost(postId, user);
        if (!liked) {
            return ResponseEntity.status(409).body("이미 좋아요를 눌렀습니다");
        }

        return ResponseEntity.ok("좋아요 완료");
    }
    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDTO> getPost(@PathVariable Long id) {
        PostResponseDTO dto = postService.getPostById(id);
        return ResponseEntity.ok(dto);
    }
}
