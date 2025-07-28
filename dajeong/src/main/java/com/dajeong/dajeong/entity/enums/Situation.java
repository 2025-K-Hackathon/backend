package com.dajeong.dajeong.entity.enums;

public enum Situation {
    HOSPITAL("병원 접수", "hospital.png"),
    SHOPPING("물건 구매", "shopping.png"),
    ADMIN_WORK("행정 업무", "admin_work.png"),
    TRANSPORT("대중 교통 이용", "transport.png"),
    KIDS("아이 교육", "kids.png"),
    EMPLOYMENT("취업", "employment.png");


    private final String description;
    private final String iconFile;

    Situation(String description, String iconFile) {
        this.description = description;
        this.iconFile = iconFile;
    }

    public String getDisplayName() {
        return description;
    }
    public String getIconFile() {
        return iconFile;
    }
}