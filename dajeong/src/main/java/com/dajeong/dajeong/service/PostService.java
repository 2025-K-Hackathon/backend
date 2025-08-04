///PostService.java

package com.dajeong.dajeong.service;

import com.dajeong.dajeong.dto.PostDetailResponseDTO;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;


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
    public void createPost(PostRequestDTO dto, List<MultipartFile> images, User user) {
        Post post = new Post();
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setNationality(dto.getNationality());
        post.setRegion(dto.getRegion());
        post.setAgeGroup(dto.getAgeGroup());
        post.setAuthor(user);

        // 이미지 저장 및 연결
        if (images != null && !images.isEmpty()) {
            if (images.size() > 3) {
                throw new IllegalArgumentException("이미지는 최대 3장까지 업로드 가능합니다.");
            }
            List<String> imageUrls = saveImages(images);
            post.setImageUrls(imageUrls);
        }

        postRepository.save(post);
    }

    private List<String> saveImages(List<MultipartFile> images) {
        List<String> imageUrls = new ArrayList<>();
        String uploadDir = "src/main/resources/static/images";

        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        for (MultipartFile image : images) {
            if (image.isEmpty()) continue;

            String originalFilename = image.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueName = UUID.randomUUID().toString() + extension;

            try {
                Path filepath = Paths.get(uploadDir, uniqueName);
                Files.write(filepath, image.getBytes());

                // URL은 클라이언트가 접근 가능한 경로로 설정 (예: /images/파일명)
                imageUrls.add("/images/" + uniqueName);
            } catch (IOException e) {
                throw new RuntimeException("이미지 저장 실패", e);
            }
        }

        return imageUrls;
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

    @Transactional(readOnly = true)
    public PostDetailResponseDTO getPostDetail(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다"));

        return PostDetailResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorName(post.getAuthor().getName())
                .nationality(post.getNationality().getDescription())
                .region(post.getRegion().getDescription())
                .ageGroup(post.getAgeGroup().getDescription())
                .likeCount(post.getLikeCount())
                .commentCount(post.getComments().size())  // 댓글 개수
                .imageUrls(post.getImageUrls())          // 이미지 리스트
                .createdAt(post.getCreatedAt())
                .build();
    }

}