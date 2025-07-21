package com.dajeong.dajeong.util;

import com.dajeong.dajeong.dto.DiaryAIResponseDTO;
import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.DeltaType;

import java.util.ArrayList;
import java.util.List;

/**
 * 원본 텍스트와 교정된 텍스트 전체를 diff-utils로 비교해
 * 틀린 단어·수정 단어·위치(start/end) 리스트를 생성한다.
 */
public class CorrectionLocator {

    public static List<DiaryAIResponseDTO.Correction> locate(
            String original,
            String fullCorrectedText) {

        /* 글자 단위 diff – 한글, 공백까지 정확히 잡기 위해 split("") */
        List<String> orig = List.of(original.split(""));
        List<String> corr = List.of(fullCorrectedText.split(""));

        List<AbstractDelta<String>> deltas =
                DiffUtils.diff(orig, corr).getDeltas();

        List<DiaryAIResponseDTO.Correction> result = new ArrayList<>();

        for (AbstractDelta<String> d : deltas) {
            if (d.getType() == DeltaType.CHANGE || d.getType() == DeltaType.DELETE) {

                int start = d.getSource().getPosition();
                int end   = start + d.getSource().size();

                String wrong = String.join("", d.getSource().getLines());
                String fixed = d.getType() == DeltaType.CHANGE
                        ? String.join("", d.getTarget().getLines())
                        : "";

                DiaryAIResponseDTO.Correction c = new DiaryAIResponseDTO.Correction();
                c.setIncorrect(wrong);
                c.setCorrected(fixed);
                c.setStart(start);
                c.setEnd(end);
                result.add(c);
            }
        }
        return result;
    }
}
