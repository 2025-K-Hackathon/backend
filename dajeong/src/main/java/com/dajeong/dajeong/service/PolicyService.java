package com.dajeong.dajeong.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class PolicyService {

    private final PythonBridgeService bridge;
    private final ObjectMapper mapper = new ObjectMapper();

    private final Map<String, String> titleUrlMap;

    public PolicyService(PythonBridgeService bridge) {
        this.bridge = bridge;
        this.titleUrlMap = loadTitleUrlMap();
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> loadTitleUrlMap() {
        try {
            Map<String, String> map1 = readSingleMappingFile("policies/notices_2025.json");
            Map<String, String> map2 = readSingleMappingFile("policies/타기관_notices_2025.json");

            // 두 맵을 병합 (중복 title이 있으면 map2 우선)
            map1.putAll(map2);
            return map1;

        } catch (IOException e) {
            throw new RuntimeException("URL mapping 파일 로드 실패", e);
        }
    }

    private Map<String, String> readSingleMappingFile(String resourcePath) throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (is == null) throw new FileNotFoundException("Mapping file not found: " + resourcePath);

        List<Map<String, String>> list = mapper.readValue(is, new TypeReference<>() {});
        return list.stream()
                .filter(m -> m.containsKey("title") && m.containsKey("url"))
                .collect(Collectors.toMap(m -> m.get("title").trim(), m -> m.get("url").trim()));
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
            Map<String, Object> result = mapper.readValue(json, new TypeReference<>() {});

            // ✅ URL 보완: URL이 없고, title이 있을 경우 보완
            if ((result.get("url") == null || String.valueOf(result.get("url")).isBlank())
                    && result.get("title") != null) {

                String title = result.get("title").toString().trim();
                if (titleUrlMap.containsKey(title)) {
                    result.put("url", titleUrlMap.get(title));
                }
            }
            return result;

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
