package com.dajeong.dajeong.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.dajeong.dajeong.entity.enums.Region;
import com.dajeong.dajeong.entity.enums.Nationality;
import com.dajeong.dajeong.entity.enums.Children;

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

    @NotNull(message = "국적을 선택하세요.")
    private Nationality nationality;

    @NotNull(message = "출생년도는 숫자만 입력해 주세요.")
    private Integer age;

    @NotNull(message = "지역을 선택하세요.")
    private Region region;

    @NotNull(message = "결혼 여부를 선택하세요.")
    private Boolean married;

    @NotNull(message = "자녀 여부를 선택하세요.")
    private Children hasChildren;

    @NotNull(message = "자녀 출생년도는 숫자만 입력해 주세요.")
    private Integer childAge;
}
