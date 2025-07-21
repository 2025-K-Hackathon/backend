package com.dajeong.dajeong.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.dajeong.dajeong.entity.Phrase;
import com.dajeong.dajeong.entity.enums.Situation;

// 상황별, 언어별 문장 조회를 위한 메소드
public interface PhraseRepository extends JpaRepository<Phrase, Long> {
    List<Phrase> findAllBySituation(Situation situation);
}
