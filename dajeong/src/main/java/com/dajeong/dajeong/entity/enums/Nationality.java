package com.dajeong.dajeong.entity.enums;

public enum Nationality {
    VIETNAM("베트남"),
    CHINA("중국"),
    PHILIPPINES("필리핀"),
    THAILAND("태국"),
    INDONESIA("인도네시아"),
    ETC("기타"),
    PRIVATE("비공개");

    private final String description;

    Nationality(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
