package com.example.demo.dto;

import lombok.Data;

@Data
public class LoginResponseDTO {
    
	private int userId;
    private String loginId;
    private String name; // 사용자 이름 (프론트엔드에서 환영 메시지 등에 사용 가능)
    private String email; // 마이페이지에서 사용할 이메일 추가
    private String token; // 인증 토큰 (JWT 등)이 들어갈 자리입니다.
}
