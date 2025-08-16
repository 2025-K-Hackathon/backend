// src/main/java/com/dajeong/dajeong/service/PolicyRecommendationService.java
package com.dajeong.dajeong.service;

import com.dajeong.dajeong.entity.PolicyRecommendation;
import com.dajeong.dajeong.entity.User;
import com.dajeong.dajeong.repository.PolicyRecommendationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PolicyRecommendationService {

    private final PolicyRecommendationRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void saveOrUpdateForDate(User user, LocalDate date, Map<String, Object> payload) {
        String json = toJson(payload);
        Optional<PolicyRecommendation> maybe = repository.findByUserAndRecommendDate(user, date);
        if (maybe.isPresent()) {
            PolicyRecommendation rec = maybe.get();
            rec.setPayloadJson(json);
            // updatedAt 컬럼이 필요하면 @UpdateTimestamp 추가
        } else {
            PolicyRecommendation rec = PolicyRecommendation.builder()
                    .user(user)
                    .recommendDate(date)
                    .payloadJson(json)
                    .build();
            repository.save(rec);
        }
    }

    @Transactional(readOnly = true)
    public boolean existsForDate(User user, LocalDate date) {
        return repository.existsByUserAndRecommendDate(user, date);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getLatestAsMap(User user) {
        return repository.findFirstByUserOrderByRecommendDateDesc(user)
                .map(rec -> fromJson(rec.getPayloadJson()))
                .orElse(Map.of("message", "추천 결과가 없습니다."));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getByDateAsMap(User user, LocalDate date) {
        return repository.findByUserAndRecommendDate(user, date)
                .map(rec -> fromJson(rec.getPayloadJson()))
                .orElse(Map.of("message", "해당 날짜에 추천 결과가 없습니다."));
    }

    private String toJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("추천 결과 직렬화 실패", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fromJson(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("추천 결과 역직렬화 실패", e);
        }
    }
}
