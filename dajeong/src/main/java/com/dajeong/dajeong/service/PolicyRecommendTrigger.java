// src/main/java/com/dajeong/dajeong/service/PolicyRecommendTrigger.java
package com.dajeong.dajeong.service;

import com.dajeong.dajeong.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

/**
 * 유저 정보를 바탕으로 프로필을 만들고 PolicyService.recommend(...)를 호출해
 * 해당 날짜의 추천을 생성/저장하는 트리거.
 */
@Service
@RequiredArgsConstructor
public class PolicyRecommendTrigger {

    private final PolicyService policyService;
    private final PolicyRecommendationService recommendationService;
    private final UserProfileBuilder userProfileBuilder; // 유저→프로필 변환 (아래 예시)

    @Transactional
    public void generateAndSaveFor(User user, LocalDate date) {
        Map<String, Object> profile = userProfileBuilder.buildFrom(user);
        Map<String, Object> result = policyService.recommend(profile);
        recommendationService.saveOrUpdateForDate(user, date, result);
    }

    // 편의: 오늘(Seoul)로 즉시 생성
    @Transactional
    public void generateAndSaveForToday(User user) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        generateAndSaveFor(user, today);
    }
}
