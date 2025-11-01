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
    // 🟢 필드 이름이 'profileImageUrl' 이어야 합니다.
    private String profileImageUrl;
    // 🟢 필드 이름이 'accessToken' 이어야 합니다.
    private String accessToken;

    private String refreshToken;
}
