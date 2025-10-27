package com.example.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data // @Getter, @Setter, @ToString, @EqualsAndHashCode 등을 포함
@NoArgsConstructor // JSON 역직렬화를 위해 기본 생성자가 필요합니다.
public class ChatRequest {
    private String prompt;
}