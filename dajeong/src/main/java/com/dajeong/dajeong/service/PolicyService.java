package com.dajeong.dajeong.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class PolicyService {

    private final PythonBridgeService bridge;
    private final ObjectMapper mapper = new ObjectMapper();

    public PolicyService(PythonBridgeService bridge) {
        this.bridge = bridge;
    }

    public Map<String, Object> recommend(Map<String, Object> userProfile) {
        final String rawOut = bridge.callWrapper("policy_recommend_wrapper.py", userProfile);
        final String trimmed = rawOut == null ? "" : rawOut.trim();

        // 1) 가장 마지막 '}' 기준으로, 앞쪽의 '{' 후보들을 뒤에서부터 시도
        final String json = extractParsableJsonBlock(trimmed);
        if (json == null) {
            String preview = previewForError(trimmed, 500);
            throw new RuntimeException("Invalid JSON from policy wrapper. " +
                    "Failed to locate parsable JSON block. stdoutPreview=" + preview);
        }

        // 2) 파싱
        try {
            return mapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            String preview = previewForError(json, 500);
            throw new RuntimeException("Invalid JSON from policy wrapper. " +
                    "Failed to parse extracted JSON block. jsonPreview=" + preview, e);
        }
    }

    /**
     * stdout에서 가장 마지막 '}'를 기준으로, 앞쪽의 '{' 후보들을 뒤에서부터 이동하며
     * JSON 파싱을 시도한다. 성공하면 그 블록을 반환, 모두 실패하면 null.
     */
    private String extractParsableJsonBlock(String s) {
        if (s == null || s.isEmpty()) return null;

        int firstOpen = s.indexOf('{');
        int lastClose = s.lastIndexOf('}');
        if (firstOpen < 0 || lastClose < 0 || lastClose <= firstOpen) return null;

        String candidate = s.substring(firstOpen, lastClose + 1);
        try {
            mapper.readTree(candidate);  // 유효성 확인
            return candidate;
        } catch (Exception e) {
            return null;
        }
    }


    private static String previewForError(String s, int maxLen) {
        if (s == null) return "<null>";
        String noCtl = s.replace("\r", " ").replace("\n", " ").replace("\t", " ").trim();
        if (noCtl.length() > maxLen) noCtl = noCtl.substring(0, maxLen) + "...";
        byte[] bytes = noCtl.getBytes(StandardCharsets.UTF_8);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
