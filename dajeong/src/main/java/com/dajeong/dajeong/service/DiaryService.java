package com.dajeong.dajeong.service;

import com.dajeong.dajeong.dto.*;
import com.dajeong.dajeong.entity.Diary;
import com.dajeong.dajeong.entity.User;
import com.dajeong.dajeong.repository.DiaryRepository;
import com.dajeong.dajeong.util.CorrectionLocator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;

    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.base}")
    private String apiBase;

    @Value("${openai.model.id}")
    private String modelId;

    @Transactional
    public void writeDiary(User user, DiaryRequestDTO dto) {
        LocalDate date = LocalDate.parse(dto.getDate());
        String content = dto.getContent();

        DiaryAIModelResultDTO ai = analyzeDiaryWithAI(content);


        List<DiaryAIResponseDTO.Correction> corrections =
                CorrectionLocator.locate(ai.getOriginalText(),
                        ai.getFullCorrectedText());

        List<String> incorrects = corrections.stream()
                .map(DiaryAIResponseDTO.Correction::getIncorrect)
                .toList();
        List<String> correcteds = corrections.stream()
                .map(DiaryAIResponseDTO.Correction::getCorrected)
                .toList();

        String correctionsJson;
        try {
            correctionsJson = objectMapper.writeValueAsString(corrections);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("corrections 직렬화 실패", e);
        }

        Diary diary = Diary.of(user, date, ai.getOriginalText());
        diary.setCorrectedText(ai.getFullCorrectedText());
        diary.setReply(ai.getReply());
        diary.setIncorrectWords(incorrects);
        diary.setCorrectedWords(correcteds);
        diary.setCorrectionsJson(correctionsJson);
        diaryRepository.save(diary);
    }


    @Transactional(readOnly = true)
    public DiaryAIResponseDTO getCorrectionsById(User user, Long id) {
        Diary diary = diaryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 일기를 찾을 수 없습니다."));
        if (!diary.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("본인의 일기만 조회할 수 있습니다.");
        }

        List<DiaryAIResponseDTO.Correction> corrections;
        try {
            corrections = Arrays.asList(
                    objectMapper.readValue(
                            diary.getCorrectionsJson(),
                            DiaryAIResponseDTO.Correction[].class
                    )
            );
        } catch (Exception e) {
            throw new RuntimeException("교정 정보 파싱 실패", e);
        }

        DiaryAIResponseDTO dto = new DiaryAIResponseDTO();
        dto.setFullCorrectedText(diary.getCorrectedText());
        dto.setReply(diary.getReply());
        dto.setCorrections(corrections);
        return dto;
    }

    @Transactional(readOnly = true)
    public DiaryResponseDTO getDiaryById(User user, Long id) {
        Diary diary = diaryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 일기를 찾을 수 없습니다."));
        if (!diary.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("본인의 일기만 조회할 수 있습니다.");
        }
        return new DiaryResponseDTO(diary);
    }

    @Transactional(readOnly = true)
    public List<DiaryResponseDTO> getDiariesByDate(User user, String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        return diaryRepository.findByUserAndDate(user, date).stream()
                .map(DiaryResponseDTO::new)
                .toList();
    }

    private DiaryAIModelResultDTO analyzeDiaryWithAI(String diaryText) {

        final String FORMAT = """
        ```json
        {
          "type":"object",
          "properties":{
            "original_text":{"type":"string"},
            "full_corrected_text":{"type":"string"},
            "reply":{"type":"string"}
          },
          "required":["original_text","full_corrected_text","reply"]
        }
        ```""";

                String prompt = String.format("""
        당신은 '다정' 서비스의 AI 상담원이자 한국어 교정 전문가입니다.
        다음 지침을 지켜 JSON 으로만 답하십시오.
        
        1. 'original_text'        : 사용자가 쓴 원본 일기
        2. 'full_corrected_text'  : 자연스럽게 교정한 문장
        3. 'reply'                : 1~3문장 공감·격려 답글
        
        %s
        
        [원본 일기]
        %s
        """, FORMAT, diaryText);


        Map<String, Object> body = Map.of(
                "model", modelId,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.7,
                "max_tokens", 1024
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);

        RestTemplate tpl = new RestTemplate();
        ResponseEntity<Map> resp = tpl.exchange(
                apiBase + "/chat/completions",
                HttpMethod.POST,
                req,
                Map.class
        );

        try {
            Map<?, ?> choice  = ((List<Map<?, ?>>) resp.getBody().get("choices")).get(0);
            Map<?, ?> message = (Map<?, ?>) choice.get("message");
            String content    = (String) message.get("content");
            String json       = extractPureJson(content);

            return objectMapper.readValue(json, DiaryAIModelResultDTO.class);

        } catch (Exception e) {
            throw new RuntimeException("AI 응답 파싱 실패: " + e.getMessage(), e);
        }
    }

    private String extractPureJson(String raw) {
        String t = raw.trim();
        if (t.startsWith("```")) {
            int s = t.indexOf('{'), e = t.lastIndexOf('}');
            if (s != -1 && e > s) return t.substring(s, e + 1);
        }
        int b = t.indexOf('{');
        if (b != -1) return t.substring(b);
        throw new IllegalArgumentException("JSON 본문을 찾을 수 없습니다.");
    }
}
