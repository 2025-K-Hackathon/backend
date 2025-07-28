package com.dajeong.dajeong.controller;

import com.dajeong.dajeong.entity.Phrase;
import com.dajeong.dajeong.service.PhraseService;
import com.dajeong.dajeong.service.TtsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tts")
@RequiredArgsConstructor
public class TtoSController {
    private final TtsService ttsService;
    private final PhraseService phraseService;

    @GetMapping(value = "/synthesize/{phraseId}", produces = "audio/mpeg")
    public ResponseEntity<byte[]> synthesizePhrase(@PathVariable("phraseId") Long phraseId) throws Exception {
        Phrase p = phraseService.findById(phraseId);
        // DB에서 해당 문장의 번역 텍스트를 가져옴
        String text = p.getTranslatedText();
        // TTS 합성
        byte[] audio = ttsService.synthesize(text, "ko-KR", "ko-KR-Standard-A");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("audio/mpeg"));
        headers.setContentDisposition(ContentDisposition.inline()
            .filename("phrase-" + phraseId + ".mp3")
            .build());

        return new ResponseEntity<>(audio, headers, HttpStatus.OK);
    }
}
