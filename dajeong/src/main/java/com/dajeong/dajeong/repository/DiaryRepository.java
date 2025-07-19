package com.dajeong.dajeong.repository;

import com.dajeong.dajeong.entity.Diary;
import com.dajeong.dajeong.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    // 특정 사용자, 특정 날짜의 일기 조회
    List<Diary> findByUserAndDate(User user, LocalDate date);
}
