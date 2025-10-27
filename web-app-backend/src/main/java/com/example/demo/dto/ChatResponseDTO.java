package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor // new ChatResponse("메시지") 처럼 쉽게 객체를 생성하기 위해 추가
public class ChatResponseDTO {
    private String response;
}