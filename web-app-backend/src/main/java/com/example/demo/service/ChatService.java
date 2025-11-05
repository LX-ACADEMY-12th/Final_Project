package com.example.demo.service;

import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List; // [!!] (추가됨) java.util.List 임포트

@Service
public class ChatService {

    private final GenerativeModel model;

    /**
     * [!!] (추가됨) AI의 역할과 행동 지침을 정의하는 '시스템 프롬프트'
     *
     * 이것이 님의 서비스 기획(초등생 과학교사)을 구현하는 핵심입니다.
     */
    private static final String SYSTEM_PROMPT =
            "당신은 'AI 과학 선생님'입니다. " +
                    "당신의 학생은 초등학교 3~6학년 학생입니다. " +
                    "모든 과학 개념을 아주 쉽고 간단한 단어로, 그리고 초등학생이 이해할 수 있는 비유와 예시를 들어 설명해야 합니다. " +
                    "답변은 항상 친절하고 다정한 선생님 말투(예: '...했단다', '...하는 거야', '...해볼까?')를 사용하세요. " +
                    "\n" +
                    "**[매우 중요한 규칙 1: 길이]**" +
                    "답변은 항상 3~4개의 문장 이내로, 아주 간단하고 명료하게 핵심만 설명하세요. " +
                    "학생이 더 궁금해하면 그때 자세히 설명하면 됩니다. 절대로 길게 말하지 마세요." +
                    "\n" +
                    "**[매우 중요한 규칙 2: 한자어]**" +
                    "학생의 질문에 '증산작용', '광합성'처럼 어려운 한자어 과학 용어가 포함되어 있다면, " +
                    "가장 먼저 그 단어의 뜻을 풀어서 설명해야 합니다. " +
                    "예를 들어, '증산작용'을 질문받으면, \"'증산'이라는 말은 '물이 흩어져 날아간다'는 뜻이야. 식물이 물을 밖으로 내보내는 거지.\"처럼 " +
                    "단어의 의미를 먼저 알려준 뒤, 과학적 원리를 설명해주세요." +
                    "\n" +
                    "**[매우 중요한 규칙 3: TTS 오류 방지]**" +
                    "답변에는 절대로 이모티콘(😊, 👍, 😂 등)을 사용하지 마세요. " +
                    "또한, 절대로 마크다운 특수문자(별표 *, 샵 #, 대시 - 등)를 꾸미기 용도로 사용하지 마세요. " +
                    "TTS가 이런 기호들을 모두 읽어버려 어색해집니다.";


    // 생성자 주입
    public ChatService(GenerativeModel model) {
        this.model = model;
    }

    /**
     * 프롬프트를 받아 AI 응답 텍스트를 반환합니다.
     * [!!] 시스템 프롬프트를 포함하도록 로직 변경
     */
    public String generateResponse(String prompt) throws IOException {

        // 1. 대화의 '맥락'을 생성합니다.
        // AI에게 역할을 부여하기 위해, 시스템 프롬프트를 '첫 번째 사용자 질문'으로 보냅니다.
        Content systemInstruction = Content.newBuilder()
                .addParts(Part.newBuilder().setText(SYSTEM_PROMPT).build())
                .setRole("user") // Vertex AI는 'system' role을 공식 지원하지 않으므로, 'user'로 시작합니다.
                .build();

        // 2. AI가 역할을 수락하는 '가짜' 응답(Model Priming)
        // 이렇게 하면 모델이 다음 'user'의 질문에 'teacher'로서 대답할 준비를 합니다.
        Content modelPrimingResponse = Content.newBuilder()
                .addParts(Part.newBuilder().setText("네, 알겠습니다. 저는 초등학생을 위한 친절한 AI 과학 선생님입니다. 무엇이든 물어보세요!").build())
                .setRole("model")
                .build();

        // 3. 실제 사용자(아이)의 질문
        Content userPrompt = Content.newBuilder()
                .addParts(Part.newBuilder().setText(prompt).build())
                .setRole("user")
                .build();

        // 4. 단일 콘텐츠가 아닌, 대화 리스트(List)를 모델에 전달
        List<Content> conversationHistory = List.of(
                systemInstruction,  // (지시) "너는 선생님이야."
                modelPrimingResponse, // (수락) "네, 저는 선생님입니다."
                userPrompt          // (질문) "증산작용이 뭐예요?"
        );

        GenerateContentResponse response = this.model.generateContent(conversationHistory);

        // 5. 응답 텍스트 추출
        String responseText = ResponseHandler.getText(response);

        return responseText;
    }
}