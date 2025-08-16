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
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE) // ← "/api" (뒤 슬래시 X)
public class PolicyController {

    private final PolicyService policyService;
    private final PolicyRecommendationService recommendationService;

    public PolicyController(PolicyService policyService,
                            PolicyRecommendationService recommendationService) {
        this.policyService = policyService;
        this.recommendationService = recommendationService;
    }

    @PostMapping("/policy/recommend")
    public ResponseEntity<?> recommend(@RequestBody Map<String, Object> profile,
                                       HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of("message", "로그인 필요"));
            }
            Map<String, Object> resp = policyService.recommend(profile);
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
