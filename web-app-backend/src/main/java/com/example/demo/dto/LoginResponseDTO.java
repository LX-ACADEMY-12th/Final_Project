package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {

	private int userId;
    private String loginId;
    private String name; // 사용자 이름 (프론트엔드에서 환영 메시지 등에 사용 가능)
    private String email; // 마이페이지에서 사용할 이메일 추가
    private String phoneNumber;
    private String gender;
    private String region;
    private String childGrade;
    
    // [기존] private String token; (삭제)

    // [변경] Access Token (API 접근용, 수명 짧음)
    private String accessToken;

    // [변경] Refresh Token (재발급용, 수명 긺)
    private String refreshToken;
}
