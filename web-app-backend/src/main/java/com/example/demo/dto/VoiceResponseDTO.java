package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoiceResponseDTO {
    private String userTranscript;  // STT
    private String aiResponseText;  // AI 답변
    private String aiResponseAudio; // TTS 음성 파일 URL
}