package com.dajeong.dajeong.entity.enums;

public enum Region {
    CAPITAL("수도권"),
    CHUNGCHEONG("충청도"),
    GANGWON("강원도"),
    GYEONGSANG("경상도"),
    JEOLLA("전라도"),
    JEJU("제주도"),
    PRIVATE("비공개");

    private final String description;

    Region(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
