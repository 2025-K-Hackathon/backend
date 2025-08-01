package com.dajeong.dajeong.service;

import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dajeong.dajeong.entity.Phrase;
import com.dajeong.dajeong.entity.User;
import com.dajeong.dajeong.entity.enums.Situation;
import com.dajeong.dajeong.external.TranslationClient;
import com.dajeong.dajeong.repository.PhraseRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import com.dajeong.dajeong.external.TranslationResult; 

@Service
@RequiredArgsConstructor
public class PhraseService {
    private final PhraseRepository phraseRepository;
    private final TranslationClient translationClient;
    private final HttpSession session;        

    public Phrase findById(Long id) {
        return phraseRepository.findById(id)
            .orElseThrow(() -> 
                new NoSuchElementException("문장 없음: " + id)
            );
    }

    @Transactional(readOnly = true)
    public List<Phrase> listByUserAndSituation(User user, Situation situation) {
        return phraseRepository.findAllBySituationAndUser(situation, user);
    }

    // 새 문장 추가
    @Transactional
    public Phrase create(Situation situation,
                         String inputLang,
                         String inputText,
                         String translatedText,
                         User user) {
        return phraseRepository.save(new Phrase(situation, inputText, translatedText, user));
    }

    // 문장 수정 (텍스트만 변경)
    @Transactional
    public Phrase update(Long id, String newText) {
        Phrase p = phraseRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("문장 없음: " + id));
        p.setInputText(newText);
        return phraseRepository.save(p);
    }

    // 문장 삭제
    @Transactional
    public void delete(Long id) {
        if (!phraseRepository.existsById(id)) {
            throw new NoSuchElementException("문장 없음: " + id);
        }
        phraseRepository.deleteById(id);
    }

    // 자동 번역 + 저장
    @Transactional
    public Phrase createWithAutoTranslate(
        Situation situation,
        String inputText
    ) {
        // 세션에서 로그인된 User 꺼내기
        User user = (User) session.getAttribute("user");
        if (user == null) {
            throw new IllegalStateException("세션에 로그인 정보가 없습니다.");
        }
        // DeepL로 자동 감지 + 한글 번역
        TranslationResult result = translationClient.translateToKorean(inputText);

        // 엔티티에 원문 언어, 원문, 번역문 저장
        Phrase p = new Phrase(
            situation,
            inputText,
            result.getTranslatedText(),
            user
        );

        return phraseRepository.save(p);
    }

    @Transactional
    public Phrase updateWithAutoTranslate(Long id, String newText) {
        // 기존 문장 조회
        Phrase p = phraseRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("문장 없음: " + id));

        // 번역
        TranslationResult result = translationClient.translateToKorean(newText);

        // 필드 갱신: inputText, translatedText
        p.setInputText(newText);
        p.setTranslatedText(result.getTranslatedText());

        // 저장 후 반환
        return phraseRepository.save(p);
    }

}
