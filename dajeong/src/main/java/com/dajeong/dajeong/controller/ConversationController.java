package com.dajeong.dajeong.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.dajeong.dajeong.dto.SituationDTO;
import com.dajeong.dajeong.dto.PhraseRequestDTO;
import com.dajeong.dajeong.dto.PhraseResponseDTO;
import com.dajeong.dajeong.entity.Phrase;
import com.dajeong.dajeong.entity.User;
import com.dajeong.dajeong.entity.enums.Situation;
import com.dajeong.dajeong.service.PhraseService;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {
    private final PhraseService phraseService;
    public ConversationController(PhraseService phraseService) {
        this.phraseService = phraseService;
    }

    // 상황 카테고리 리스트 조회
    @GetMapping
    public List<SituationDTO> listCategories() {
        return Arrays.stream(Situation.values())
            .map(ct -> new SituationDTO(ct.name(), ct.getDisplayName()))
            .collect(Collectors.toList());
    }

    // 상황 카테고리별 문장 리스트 조회
    @GetMapping("/{type}")
    public List<PhraseResponseDTO> listPhrases(
        @PathVariable("type") String type // URL의 {type} 부분을 문자열로 받음
    ) {
        // 들어오는 type 문자열을 Situation enum으로 변환해서 situation 변수에 담기
        Situation situation = Situation.valueOf(type.toUpperCase());
        List<Phrase> phrases = phraseService.list(situation);

        if (phrases == null || phrases.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "해당 카테고리에 회화 문장이 없습니다."
            );
    }
        return phrases.stream()
            .map(PhraseResponseDTO::fromEntity)
            .collect(Collectors.toList());
    }

    // 문장 추가
    @PostMapping("/{type}")
    @ResponseStatus(HttpStatus.CREATED)
    public PhraseResponseDTO createPhrase(
        @PathVariable("type") String type,
        @RequestBody PhraseRequestDTO dto,
        HttpSession session
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 필요");
        }
        Situation situation = Situation.valueOf(type.toUpperCase());
        Phrase p = phraseService.create(
            situation,
            dto.inputLang(),
            dto.inputText(),
            dto.translatedText()
        );
        return PhraseResponseDTO.fromEntity(p);
    }
    
    // 문장 수정
    @PutMapping("/{type}/{id}")
    public PhraseResponseDTO updatePhrase(
        @PathVariable("type") String type,
        @PathVariable("id") Long id,
        @RequestBody PhraseRequestDTO dto,
        HttpSession session
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 필요");
        }
        // type 검증만, service는 id 기준으로 수정
        var p = phraseService.update(id, dto.inputText());
        return PhraseResponseDTO.fromEntity(p);
    }

    // 문장 삭제
    @DeleteMapping("/{type}/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePhrase(
        @PathVariable("type") String type,
        @PathVariable("id") Long id,
        HttpSession session
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 필요");
        }
        phraseService.delete(id);
    }
}
