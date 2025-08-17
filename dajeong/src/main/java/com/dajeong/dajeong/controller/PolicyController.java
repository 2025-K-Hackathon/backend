// src/main/java/com/dajeong/dajeong/controller/PolicyController.java
package com.dajeong.dajeong.controller;

import com.dajeong.dajeong.entity.User;
import com.dajeong.dajeong.service.PolicyRecommendationService;
import com.dajeong.dajeong.service.PolicyService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class PolicyController {

    private final PolicyService policyService;
    private final PolicyRecommendationService recommendationService;


    public PolicyController(PolicyService policyService,
                            PolicyRecommendationService recommendationService) {
        this.policyService = policyService;
        this.recommendationService = recommendationService;
    }
    @PostMapping(value = "/policy/recommend", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> recommend(
            @RequestBody(required = false) Map<String, Object> profile,
            HttpSession session
    ) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of("message", "로그인 필요"));
            }

            if (profile == null || profile.isEmpty()) {
                // 세션/DB의 사용자 정보로 자동 구성
                profile =  Map.of(
                        "name", user.getName(),
                        "nationality", user.getNationality(),
                        "age", user.getAge(),           // 예: 1998
                        "childAge", user.getChildAge(), // 예: 2020
                        "region", user.getRegion(),
                        "married", user.getMarried(),
                        "hasChildren", user.getHasChildren()
                );
            }
            Map<String, Object> resp = policyService.recommend(profile);
            System.out.println("[FastAPI 응답 전체 확인]");
            System.out.println(new com.fasterxml.jackson.databind.ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(resp));
            LocalDate todaySeoul = LocalDate.now(ZoneId.of("Asia/Seoul"));
            recommendationService.saveOrUpdateForDate(user, todaySeoul, resp);

            return ResponseEntity.ok(resp);

        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "Internal Server Error", "message", e.getMessage())
            );
        }
    }
    @GetMapping("/policy/recommendations/latest")
    public ResponseEntity<?> getLatest(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).body(Map.of("message", "로그인 필요"));
        return ResponseEntity.ok(recommendationService.getLatestAsMap(user));
    }
}
