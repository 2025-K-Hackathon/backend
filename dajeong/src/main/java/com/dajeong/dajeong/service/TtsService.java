package com.dajeong.dajeong.service;
import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TtsService {
    public byte[] synthesize(String text, String languageCode, String voiceName) throws Exception {
        try (TextToSpeechClient client = TextToSpeechClient.create()) {
            // 1) 입력 텍스트
            SynthesisInput input = SynthesisInput.newBuilder()
                .setText(text)
                .build();

            // 2) 목소리 설정
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                .setLanguageCode(languageCode) 
                .setName(voiceName)    
                .build();

            // 3) MP3 포맷
            AudioConfig audioConfig = AudioConfig.newBuilder()
                .setAudioEncoding(AudioEncoding.MP3)
                .build();

            // 4) 요청 및 응답
            SynthesizeSpeechResponse response = client.synthesizeSpeech(input, voice, audioConfig);
            ByteString audioContents = response.getAudioContent();
            return audioContents.toByteArray();  // byte[] 반환
        }
    }
}