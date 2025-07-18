package com.dajeong.dajeong.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String nationality;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private Boolean married;

    @Column(nullable = false)
    private Boolean hasChildren;
}
