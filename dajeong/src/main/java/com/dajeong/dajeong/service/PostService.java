///PostService.java

package com.dajeong.dajeong.service;

import com.dajeong.dajeong.dto.PostRequestDTO;
import com.dajeong.dajeong.dto.PostResponseDTO;
import com.dajeong.dajeong.entity.Post;
import com.dajeong.dajeong.entity.PostLike;
import com.dajeong.dajeong.entity.User;
import com.dajeong.dajeong.repository.PostLikeRepository;
import com.dajeong.dajeong.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    public boolean likePost(Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다."));
        // 좋아요 중복처리 로직
        if (postLikeRepository.existsByUserAndPost(user, post)) {
            return false;
        }
        PostLike like = PostLike.builder()
                .user(user)
                .post(post)
                .build();
        postLikeRepository.save(like);
        post.setLikeCount(post.getLikeCount() + 1);
        postRepository.save(post);
        return true;
    }

    public void createPost(PostRequestDTO dto, User user) {
        Post post = new Post();
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setNationality(dto.getNationality());
        post.setRegion(dto.getRegion());
        post.setAgeGroup(dto.getAgeGroup());
        post.setAuthor(user);
        postRepository.save(post);
    }

    public List<PostResponseDTO> getFilteredPosts(
            String nationality,
            String region,
            String ageGroup,
            String keyword,
            String sort
    ) {
        List<Post> posts = postRepository.findAll();
        Stream<Post> stream = posts.stream();

        if (nationality != null) stream = stream.filter(p -> p.getNationality().equals(nationality));
        if (region      != null) stream = stream.filter(p -> p.getRegion().equals(region));
        if (ageGroup    != null) stream = stream.filter(p -> p.getAgeGroup().equals(ageGroup));
        if (keyword     != null) stream = stream.filter(p ->
                p.getTitle().contains(keyword) || p.getContent().contains(keyword)
        );

        List<Post> filtered = "popular".equals(sort)
                ? stream.sorted(Comparator.comparing(Post::getLikeCount).reversed()).toList()
                : stream.sorted(Comparator.comparing(Post::getCreatedAt).reversed()).toList();

        return filtered.stream()
                .map(post -> new PostResponseDTO(
                        post.getId(),
                        post.getTitle(),
                        post.getContent(),
                        post.getAuthor().getName(),   // 작성자 이름
                        post.getNationality(),
                        post.getRegion(),
                        post.getAgeGroup(),
                        post.getLikeCount(),
                        post.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    public boolean deletePost(Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다"));

        // 작성자와 요청자가 다르면 false 반환
        if (!post.getAuthor().getId().equals(user.getId())) {
            return false;
        }

        postRepository.delete(post);
        return true;
    }
    public PostResponseDTO getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다"));
        return new PostResponseDTO(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthor().getName(),
                post.getNationality(),
                post.getRegion(),
                post.getAgeGroup(),
                post.getLikeCount(),
                post.getCreatedAt()
        );
    }

}
