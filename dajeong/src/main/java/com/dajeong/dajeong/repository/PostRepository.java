///PostRepository.java

package com.dajeong.dajeong.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dajeong.dajeong.entity.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
}
