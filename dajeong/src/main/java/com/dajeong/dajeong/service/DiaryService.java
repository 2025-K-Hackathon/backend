package com.dajeong.dajeong.service;

import com.dajeong.dajeong.dto.DiaryRequestDTO;
import com.dajeong.dajeong.dto.DiaryResponseDTO;
import com.dajeong.dajeong.entity.Diary;
import com.dajeong.dajeong.entity.User;
import com.dajeong.dajeong.repository.DiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiaryService {
    private final DiaryRepository diaryRepository;

    @Transactional
    public void writeDiary(User user, DiaryRequestDTO dto) {
        LocalDate date = LocalDate.parse(dto.getDate());
        Diary diary = Diary.of(user, date, dto.getContent());
        diaryRepository.save(diary);
    }

    @Transactional(readOnly = true)
    public List<DiaryResponseDTO> getDiariesByDate(User user, String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        List<Diary> list = diaryRepository.findByUserAndDate(user, date);
        return list.stream()
                .map(d -> new DiaryResponseDTO(d.getId(), d.getDate().toString(), d.getContent()))
                .collect(Collectors.toList());
    }
}
