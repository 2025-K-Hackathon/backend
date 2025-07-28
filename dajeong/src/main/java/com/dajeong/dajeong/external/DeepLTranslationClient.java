package com.dajeong.dajeong.external;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.Map;
import java.util.List;

@Service
public class DeepLTranslationClient implements TranslationClient {
    private final WebClient wc;

    public DeepLTranslationClient(
        WebClient.Builder builder,
        @Value("${deepl.api.key}") String apiKey
    ) {
        this.wc = builder
            .baseUrl("https://api-free.deepl.com/v2")
            .defaultHeader("Authorization", "DeepL-Auth-Key " + apiKey)
            .build();
    }

    @Override
    public TranslationResult translateToKorean(String text) {
        Mono<Map<String, Object>> respMono = wc.post()
            .uri("/translate")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData("text", text)
                    .with("target_lang", "KO")
            )
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<>() { });

        Map<String, Object> body = respMono.block();
        List<?> translations = (List<?>) body.get("translations");
        if (translations.isEmpty()) {
            throw new RuntimeException("DeepL 번역 결과 없음");
        }
        Map<?,?> first = (Map<?,?>) translations.get(0);
        String translated = (String) first.get("text");
        String detected  = (String) first.get("detected_source_language");

        return new TranslationResult(translated, detected);
    }
}
