package com.example.demo.config; // (패키지 경로는 맞게 수정하세요)

import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class GcpConfig {

    @Bean
    public SpeechClient speechClient() throws IOException {
        // GOOGLE_APPLICATION_CREDENTIALS 환경변수를 통해 인증 정보를 자동으로 읽어옵니다.
        return SpeechClient.create();
    }

    @Bean
    public TextToSpeechClient textToSpeechClient() throws IOException {
        // GOOGLE_APPLICATION_CREDENTIALS 환경변수를 통해 인증 정보를 자동으로 읽어옵니다.
        return TextToSpeechClient.create();
    }

    @Bean
    public Storage storage() throws IOException {
        // GOOGLE_APPLICATION_CREDENTIALS 인증 정보를 자동으로 읽어옵니다.
        return StorageOptions.getDefaultInstance().getService();
    }
}