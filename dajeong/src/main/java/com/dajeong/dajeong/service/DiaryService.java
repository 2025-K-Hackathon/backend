package com.dajeong.dajeong.service;

import com.dajeong.dajeong.dto.*;
import com.dajeong.dajeong.entity.Diary;
import com.dajeong.dajeong.entity.User;
import com.dajeong.dajeong.entity.enums.Nationality;
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
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
                CorrectionLocator.locate(
                        Optional.ofNullable(ai.getOriginalText())
                                .orElseThrow(() -> new IllegalArgumentException("❗ originalText가 null입니다")),
                        Optional.ofNullable(ai.getFullCorrectedText())
                                .orElseThrow(() -> new IllegalArgumentException("❗ fullCorrectedText가 null입니다"))
                );


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
        dto.setOriginalText(diary.getContent());
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

        String prompt = String.format("""
        당신은 '다정' 서비스의 AI 상담원이자 한국어 교정 전문가입니다.
        다음 지침을 철저히 지키고, 반드시 **JSON 형식으로만** 응답하세요.
    
        * 출력 형식 (반드시 아래 JSON처럼 큰따옴표 사용):
    
        {
          "original_text": "일기 원문",
          "full_corrected_text": "교정된 일기",
          "reply": "격려 메시지"
        }
    
        ⚠️ 반드시 아래 조건을 지키세요:
        - 모든 키와 문자열 값은 반드시 큰따옴표 (")로 감싸야 합니다.
        - 모든 필드는 반드시 포함되어야 하며, `null`, 빈 문자열, 생략 없이 값을 채우세요.
        - 절대로 `original_text: ...` 같은 YAML 스타일로 작성하지 마세요.
        - JSON 외에 설명, 주석, 텍스트 등을 포함하지 마세요.
    
        [원본 일기]
        %s
        """, diaryText);


        Map<String, Object> body = Map.of(
                "model", modelId,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.7,
                "max_tokens", 1024
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);

        RestTemplate tpl = new RestTemplate();
        ResponseEntity<Map> resp = tpl.exchange(
                apiBase + "/chat/completions",
                HttpMethod.POST,
                req,
                Map.class
        );

        try {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) resp.getBody().get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = (String) message.get("content");

            System.out.println("🟡 GPT 응답 원문 ↓↓↓");
            System.out.println(content);

            // JSON만 추출
            String jsonStr = extractPureJson(content);
            System.out.println("🟢 추출된 JSON ↓↓↓");
            System.out.println(jsonStr);

            // 파싱
            DiaryAIModelResultDTO parsed = objectMapper.readValue(jsonStr, DiaryAIModelResultDTO.class);

            // 각 필드 로그 출력
            System.out.println("🔵 originalText: " + parsed.getOriginalText());
            System.out.println("🔵 fullCorrectedText: " + parsed.getFullCorrectedText());
            System.out.println("🔵 reply: " + parsed.getReply());

            // null 필드 확인
            if (parsed.getOriginalText() == null || parsed.getFullCorrectedText() == null || parsed.getReply() == null) {
                throw new RuntimeException("GPT 응답에 누락된 필드가 있습니다. 응답 내용 확인 필요:\n" + jsonStr);
            }

            return parsed;

        } catch (Exception e) {
            throw new RuntimeException("AI 응답 파싱 실패: " + e.getMessage(), e);
        }
    }


    @Transactional(readOnly = true)
    public String translateReply(User user, Long diaryId) {
        if (user.getNationality() == null || user.getNationality() == Nationality.PRIVATE) {
            return "국적이 설정되어 있지 않습니다.";
        }

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new RuntimeException("해당 일기를 찾을 수 없습니다."));

        if (!diary.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("본인의 일기만 번역할 수 있습니다.");
        }

        String reply = diary.getReply();
        if (reply == null || reply.isBlank()) {
            return "번역할 답변이 없습니다.";
        }

        // 모든 국적자에게 → "한국어 → 해당 언어" 번역
        // 예시에서는 한국어 → 베트남어 등
        String targetLang = switch (user.getNationality()) {
            case VIETNAM -> "베트남어";
            case CHINA -> "중국어";
            case PHILIPPINES -> "영어";
            case THAILAND -> "태국어";
            case INDONESIA -> "인도네시아어";
            case ETC -> "사용자의 모국어";
            default -> throw new IllegalArgumentException("번역 대상 언어를 알 수 없습니다.");
        };

        String prompt = String.format("""
        다음 문장을 %s로 자연스럽게 번역하고, 번역된 문장만 출력하세요. 
        설명이나 예시 절대 포함하지 마세요. 
        그저 번역된 문장만 출력하세요:
        
        %s
        """, targetLang, reply);


        Map<String, Object> body = Map.of(
                "model", modelId,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.7,
                "max_tokens", 512
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
            return ((String) message.get("content")).trim();
        } catch (Exception e) {
            throw new RuntimeException("번역 결과 파싱 실패: " + e.getMessage(), e);
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
