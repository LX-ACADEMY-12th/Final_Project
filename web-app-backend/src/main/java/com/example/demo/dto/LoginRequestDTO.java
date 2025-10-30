package com.example.demo.dto;

import lombok.Data;

//@Data 어노테이션은 Getter, Setter 등을 자동으로 생성합니다.
@Data
public class LoginRequestDTO {
 
	 private String loginId; // 프론트엔드에서 'id'로 넘어오는 필드는 DB의 'login_id'에 해당합니다.
	 private String password; // 비밀번호 필드입니다.
}
