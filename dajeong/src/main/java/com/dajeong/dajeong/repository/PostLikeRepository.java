package com.dajeong.dajeong.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import com.dajeong.dajeong.entity.PostLike;
import com.dajeong.dajeong.entity.User;
import com.dajeong.dajeong.entity.Post;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    boolean existsByUserAndPost(User user, Post post);
    Optional<PostLike> findByUserAndPost(User user, Post post);
}
