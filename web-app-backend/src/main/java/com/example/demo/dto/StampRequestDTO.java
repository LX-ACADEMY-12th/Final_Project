package com.example.demo.dto;

import java.math.BigDecimal;

import lombok.Data;

// 사용자의 요청
@Data
public class StampRequestDTO {

	private Long userId;
	private String targetType;
	private Long targetId;
//	private String authMethod;
	
	// 사용자의 현재 위치
	private BigDecimal latitude;
	private BigDecimal longitude;
	
}
