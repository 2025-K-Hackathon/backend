// src/main/java/com/dajeong/dajeong/scheduler/PolicyRecommendationScheduler.java
package com.dajeong.dajeong.scheduler;

import com.dajeong.dajeong.entity.User;
import com.dajeong.dajeong.repository.UserRepository;
import com.dajeong.dajeong.service.PolicyRecommendTrigger;
import com.dajeong.dajeong.service.PolicyRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PolicyRecommendationScheduler {

    private final UserRepository userRepository;
    private final PolicyRecommendTrigger recommendTrigger;
    private final PolicyRecommendationService recommendationService;

    // 매일 00:00 (Asia/Seoul) 실행
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void runNightly() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        List<User> users = userRepository.findAll(); // 필요 시 '활성 유저'로 제한
        for (User user : users) {
            // 같은 날짜의 결과가 이미 있으면 스킵 (중복 방지)
            if (recommendationService.existsForDate(user, today)) continue;

            // 유저 기반 프로필로 추천 생성 & 저장
            recommendTrigger.generateAndSaveFor(user, today);
        }
    }
}
