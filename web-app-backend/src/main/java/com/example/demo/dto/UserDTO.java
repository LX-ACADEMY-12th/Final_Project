package com.example.demo.dto;

import lombok.Data;

// @Data 어노테이션은 자동으로 Getter, Setter, toString(), equals(), hashCode() 메서드를 생성해줍니다.
@Data
public class UserDTO {

    private int userId; // '사용자 ID (PK)'에 해당하는 필드입니다.
    private String loginId; // '로그인 아이디'에 해당하는 필드입니다.
    private String password; // '비밀번호'에 해당하는 필드입니다.
    private String name; // '이름'에 해당하는 필드입니다.
    private String email; // '이메일'에 해당하는 필드입니다.
    private String phoneNumber; // '연락처'에 해당하는 필드입니다.
    private String gender; // '성별'에 해당하는 필드입니다.
    private String region; // 용어집의 '지역'에 해당하는 필드입니다.
    private String childGrade; // 용어집의 '자녀 학년'에 해당하는 필드입니다.

    // 🟢 [추가] 프로필 이미지 URL (또는 GCS Blob 이름)
    // GCS에 업로드된 프로필 이미지의 경로를 저장하기 위한 필드입니다.
    private String profileImageUrl;
}