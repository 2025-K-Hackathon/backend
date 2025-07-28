///PostService.java

package com.dajeong.dajeong.service;

import com.dajeong.dajeong.dto.PostRequestDTO;
import com.dajeong.dajeong.dto.PostResponseDTO;
import com.dajeong.dajeong.entity.Post;
import com.dajeong.dajeong.entity.PostLike;
import com.dajeong.dajeong.entity.User;
import com.dajeong.dajeong.entity.enums.Nationality;
import com.dajeong.dajeong.entity.enums.Region;
import com.dajeong.dajeong.entity.enums.AgeGroup;
import com.dajeong.dajeong.repository.PostLikeRepository;
import com.dajeong.dajeong.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    @Transactional
    public boolean likePost(Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다."));
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

    @Transactional
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

    @Transactional(readOnly = true)
    public List<PostResponseDTO> getFilteredPosts(
            String nationality,
            String region,
            String ageGroup,
            String keyword,
            String sort
    ) {
        Stream<Post> stream = postRepository.findAll().stream();

        if (nationality != null) {
            Nationality natEnum = Nationality.valueOf(nationality);
            stream = stream.filter(p -> p.getNationality() == natEnum);
        }
        if (region != null) {
            Region regEnum = Region.valueOf(region);
            stream = stream.filter(p -> p.getRegion() == regEnum);
        }
        if (ageGroup != null) {
            AgeGroup ageEnum = AgeGroup.valueOf(ageGroup);
            stream = stream.filter(p -> p.getAgeGroup() == ageEnum);
        }
        if (keyword != null && !keyword.isBlank()) {
            stream = stream.filter(p ->
                    p.getTitle().contains(keyword) || p.getContent().contains(keyword)
            );
        }

        List<Post> filtered = "popular".equals(sort)
                ? stream.sorted(Comparator.comparing(Post::getLikeCount).reversed()).toList()
                : stream.sorted(Comparator.comparing(Post::getCreatedAt).reversed()).toList();

        return filtered.stream()
                .map(post -> new PostResponseDTO(
                        post.getId(),
                        post.getTitle(),
                        post.getContent(),
                        post.getAuthor().getName(),
                        post.getNationality().getDescription(),  // 한글 설명 반환
                        post.getRegion().getDescription(),
                        post.getAgeGroup().getDescription(),
                        post.getLikeCount(),
                        post.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean deletePost(Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다"));
        if (!post.getAuthor().getId().equals(user.getId())) {
            return false;
        }
        postRepository.delete(post);
        return true;
    }

    @Transactional(readOnly = true)
    public PostResponseDTO getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다"));
        return new PostResponseDTO(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthor().getName(),
                post.getNationality().getDescription(),
                post.getRegion().getDescription(),
                post.getAgeGroup().getDescription(),
                post.getLikeCount(),
                post.getCreatedAt()
        );
    }
}
