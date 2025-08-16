// src/main/java/com/dajeong/dajeong/repository/PolicyRecommendationRepository.java
package com.dajeong.dajeong.repository;

import com.dajeong.dajeong.entity.PolicyRecommendation;
import com.dajeong.dajeong.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface PolicyRecommendationRepository extends JpaRepository<PolicyRecommendation, Long> {
    boolean existsByUserAndRecommendDate(User user, LocalDate date);
    Optional<PolicyRecommendation> findByUserAndRecommendDate(User user, LocalDate date);
    Optional<PolicyRecommendation> findFirstByUserOrderByRecommendDateDesc(User user);
}
