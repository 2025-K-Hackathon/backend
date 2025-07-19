package com.dajeong.dajeong.entity.enums;

public enum AgeGroup {
    TWENTIES("20대"),
    THIRTIES("30대"),
    FORTIES("40대"),
    FIFTIES("50대"),
    SIXTIES_OVER("60세 이상"),
    PRIVATE("비공개");

    private final String description;

    AgeGroup(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
