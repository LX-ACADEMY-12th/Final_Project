package com.example.demo.service;

import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ChatService {

    private final GenerativeModel model;

    // 생성자 주입
    public ChatService(GenerativeModel model) {
        this.model = model;
    }

    /**
     * 프롬프트를 받아 AI 응답 텍스트를 반환합니다.
     */
    public String generateResponse(String prompt) throws IOException {
        // 1. 모델에 보낼 콘텐츠(프롬프트) 생성
        Content content = Content.newBuilder()
                .addParts(Part.newBuilder().setText(prompt).build())
                .setRole("user")
                .build();

        // 2. 모델 호출
        GenerateContentResponse response = this.model.generateContent(content);

        // 3. 응답 텍스트 추출
        // (참고: 안전 설정 등으로 응답이 차단되면 ResponseHandler가 예외를 던질 수 있습니다)
        String responseText = ResponseHandler.getText(response);

        return responseText;
    }
}