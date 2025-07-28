package com.dajeong.dajeong.entity.enums;

public enum Children {
    YES("있음"),
    NO("없음"),
    PRIVATE("비공개");

    private final String description;

    Children(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
