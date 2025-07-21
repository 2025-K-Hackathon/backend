///DiaryController.java

package com.dajeong.dajeong.controller;

import com.dajeong.dajeong.dto.DiaryAIResponseDTO;
import com.dajeong.dajeong.dto.DiaryRequestDTO;
import com.dajeong.dajeong.dto.DiaryResponseDTO;
import com.dajeong.dajeong.entity.User;
import com.dajeong.dajeong.service.DiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    /// 일기 작성
    @PostMapping
    public void writeDiary(@RequestBody DiaryRequestDTO dto,
                           HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) throw new RuntimeException("로그인이 필요합니다.");
        diaryService.writeDiary(user, dto);
    }

    /// 날짜별 일기 조회
    @GetMapping
    public List<DiaryResponseDTO> getByDate(@RequestParam String date,
                                            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) throw new RuntimeException("로그인이 필요합니다.");
        return diaryService.getDiariesByDate(user, date);
    }

    ///특정 일기 조회
    @GetMapping("/{id}")
    public ResponseEntity<DiaryResponseDTO> getDiaryById(@PathVariable Long id,
                                                         HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) throw new RuntimeException("로그인이 필요합니다.");
        DiaryResponseDTO dto = diaryService.getDiaryById(user, id);
        return ResponseEntity.ok(dto);
    }

    ///특정 일기 교정/조언 조회
    @GetMapping("/{id}/correct")
    public ResponseEntity<DiaryAIResponseDTO> correctDiaryById(
            @PathVariable Long id,
            HttpSession session
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }
        DiaryAIResponseDTO result = diaryService.getCorrectionsById(user, id);
        return ResponseEntity.ok(result);
    }

}
