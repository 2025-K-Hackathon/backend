package com.dajeong.dajeong.entity;

//JPA에서 Entity, Id, Column, GenerationType 등 관련 기능 사용
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import jakarta.persistence.Column;
// Lombok 라이브러리 - getter/setter, 기본 생성자 자동 생성

@Getter @Setter @Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Entity(name = "users")
public class User {
    @Id // primary key(기본 키)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB 숫자 자동 증가
    private Long id; // 유저 고유 ID (자동 생성)

    @Column(unique = true)
    private String username;
    
    private String password;

    private String name;   
    private String nationality; 
    private int age;     
    private String region; 
    private Boolean married;   // 결혼 여부 (예/아니오)
    private Boolean hasChildren; // 자녀 유무 (예/아니오)
}
