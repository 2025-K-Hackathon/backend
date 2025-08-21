package com.dajeong.dajeong.entity;
import com.dajeong.dajeong.entity.enums.Region;
import com.dajeong.dajeong.entity.enums.Nationality;
import com.dajeong.dajeong.entity.enums.Children;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Nationality nationality;

    @Column(name = "age", nullable = false)
    private Integer age;

    @Column(name = "child_age", nullable = false)
    private Integer childAge;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Region region;

    @Column(nullable = false)
    private Boolean married;

    @Enumerated(EnumType.STRING)
    @Column(name = "has_children", nullable = false)
    private Children hasChildren;
}
