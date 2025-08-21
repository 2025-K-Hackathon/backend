// src/main/java/com/dajeong/dajeong/entity/PolicyRecommendation.java
package com.dajeong.dajeong.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "policy_recommendation",
        uniqueConstraints = @UniqueConstraint(name="uk_user_date", columnNames = {"user_id","recommend_date"}))
public class PolicyRecommendation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="user_id")
    private User user;

    @Column(name = "recommend_date", nullable = false)
    private LocalDate recommendDate;

    @Lob
    @Column(name = "payload_json", columnDefinition = "LONGTEXT", nullable = false)
    private String payloadJson; // 추천 결과(JSON 문자열)

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
