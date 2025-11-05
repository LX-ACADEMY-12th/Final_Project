package com.example.demo.controller;

import com.example.demo.dto.VoiceResponseDTO;
import com.example.demo.service.VoiceQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class VoiceQueryController {

    private final VoiceQueryService voiceQueryService;

    public VoiceQueryController(VoiceQueryService voiceQueryService) {
        this.voiceQueryService = voiceQueryService;
    }

    /**
     * 음성 UI와 통신하는 메인 API
     * @param audioFile (Vue에서 'audio'라는 키로 보낸 FormData)
     */
    @PostMapping("/voice-query")
    public ResponseEntity<VoiceResponseDTO> handleVoiceQuery(@RequestParam("audio") MultipartFile audioFile) {

        if (audioFile.isEmpty()) {
            return ResponseEntity.badRequest().body(new VoiceResponseDTO("Audio file is empty", null, null));
        }

        try {
            VoiceResponseDTO response = voiceQueryService.processVoiceQuery(audioFile);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace(); // 로깅
            return ResponseEntity.status(500)
                    .body(new VoiceResponseDTO("Error processing audio", e.getMessage(), null));
        }
    }
}