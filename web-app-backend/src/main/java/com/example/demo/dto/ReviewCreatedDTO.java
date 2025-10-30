package com.example.demo.dto;

import java.math.BigDecimal;

import lombok.Data;


@Data
public class ReviewCreatedDTO {

	// 1. 무엇에 대한 리뷰인지(필수)
	private Long targetId;
	private String targetType;
	
	// 2. 사용자가 입력한 리뷰 내용(필수)
	private String content;
	private BigDecimal rating;
	
	// MyBatis가 생성된 PK를 반환하기 위해 필요한 필드
    // 이 필드는 클라이언트가 보낼 필요는 없습니다 (null).
	private Long reviewId;
}