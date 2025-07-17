package com.dajeong.dajeong.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignupDTO {
    private long id;

    @NotBlank(message = "아이디를 입력하세요.")
    private String username;

    @NotBlank(message = "비밀번호를 입력하세요.")
    private String password;

    @NotBlank(message = "이름을 입력하세요.")
    private String name;

    @NotBlank(message = "국적을 선택하세요.")
    private String nationality;

    @Min(value = 1, message = "나이는 1 이상이어야 합니다.")
    private int age;

    @NotBlank(message = "지역을 선택하세요.")
    private String region;

    @NotNull(message = "결혼 여부를 선택하세요.")
    private Boolean married;

    @NotNull(message = "자녀 여부를 선택하세요.")
    private Boolean hasChildren;
}
