package com.dajeong.dajeong.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserlistDTO {
    private long id;
    private String username;
    private String name;
    private String nationality;
    private int age;
    private String region;
    private Boolean married;
    private Boolean hasChildren;
}