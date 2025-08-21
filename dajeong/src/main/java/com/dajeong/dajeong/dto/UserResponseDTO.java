package com.dajeong.dajeong.dto;
import com.dajeong.dajeong.entity.User;
import com.dajeong.dajeong.entity.enums.Nationality;
import com.dajeong.dajeong.entity.enums.Region;
import com.dajeong.dajeong.entity.enums.Children;

public record UserResponseDTO(
    Long     id,
    String   username,
    String   name,
    Nationality nationality,
    Integer age,
    Integer childAge,
    Region   region,
    Boolean  married,
    Children  hasChildren
) {
    public static UserResponseDTO fromEntity(User u) {
        return new UserResponseDTO(
            u.getId(),
            u.getUsername(),
            u.getName(),
            u.getNationality(),
            u.getAge(),
            u.getChildAge(),
            u.getRegion(),
            u.getMarried(),
            u.getHasChildren()
        );
    }
}