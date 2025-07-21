package com.dajeong.dajeong.entity.enums;

public enum Situation {
    HOSPITAL("병원 접수"),
    SHOPPING("물건 구매"),
    ADMIN_WORK("행정 업무"),
    TRANSPORT("대중 교통 이용"),
    OTHER("기타");


    private final String description;

    Situation(String description) {
        this.description = description;
    }

    public String getDisplayName() {
        return description;
    }
}