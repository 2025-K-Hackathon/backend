///DiaryController.java

package com.dajeong.dajeong.controller;

import com.dajeong.dajeong.dto.DiaryRequestDTO;
import com.dajeong.dajeong.dto.DiaryResponseDTO;
import com.dajeong.dajeong.entity.User;
import com.dajeong.dajeong.service.DiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    @PostMapping
    public void writeDiary(@RequestBody DiaryRequestDTO dto,
                           HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }
        diaryService.writeDiary(user, dto);
    }

    @GetMapping
    public List<DiaryResponseDTO> getByDate(@RequestParam String date,
                                            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }
        return diaryService.getDiariesByDate(user, date);
    }
}
