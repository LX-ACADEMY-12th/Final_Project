package com.example.demo.controller;

import com.example.demo.dto.ChatRequest;
import com.example.demo.dto.ChatResponse;
import com.example.demo.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Vue.js 프론트엔드와 통신하는 메인 채팅 API
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        try {
            // 서비스 로직을 호출하여 AI 응답을 받음
            String aiMessage = chatService.generateResponse(request.getPrompt());

            // DTO에 담아 JSON으로 반환
            return ResponseEntity.ok(new ChatResponse(aiMessage));

        } catch (IOException e) {
            // Vertex AI SDK 호출 중 I/O 오류 발생 시
            e.printStackTrace(); // 실제 운영 환경에서는 로깅(logging)을 사용해야 합니다.
            return ResponseEntity.status(500)
                    .body(new ChatResponse("AI 서버와 통신 중 오류가 발생했습니다."));
        } catch (Exception e) {
            // 기타 예외 처리 (예: Gemini 응답이 차단된 경우)
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(new ChatResponse("응답 생성 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}