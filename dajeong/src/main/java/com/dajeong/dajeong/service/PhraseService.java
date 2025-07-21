package com.dajeong.dajeong.service;

import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dajeong.dajeong.entity.Phrase;
import com.dajeong.dajeong.entity.enums.Situation;
import com.dajeong.dajeong.repository.PhraseRepository;

@Service
public class PhraseService {
    private final PhraseRepository phraseRepository;

    public PhraseService(PhraseRepository phraseRepository) {
        this.phraseRepository = phraseRepository;
    }

    // 카테고리 내 문장 리스트 조회
    @Transactional(readOnly = true)
    public List<Phrase> list(Situation situation) {
        return phraseRepository.findAllBySituation(situation);
    }

    // 새 문장 추가
    @Transactional
    public Phrase create(Situation situation,
                         String inputLang,
                         String inputText,
                         String translatedText) {
        return phraseRepository.save(new Phrase(situation, inputLang, inputText, translatedText));
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
}
