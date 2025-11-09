package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

@Data
public class ReportedReviewDTO {
	
	// --- 1. review 테이블의 기본 정보 (관리자 화면 AdminReviews.vue에 표시될 정보) ---
	private Long reviewId;
	private String targetType; 
	private Long targetId;
	private String content; // 후기 내용
	private BigDecimal rating; // 별점
	private Date createdAt; // 작성일 (AdminReviews.vue의 date)
	
	// --- 2. user 테이블 정보 (작성자 정보) ---
	private String authorName; // 작성자 이름 (AdminReviews.vue의 author)
	private Long authorId;
	
	// --- 3. review_report 테이블에서 집계된 정보 (핵심 추가 정보) ---
	private int reportCount; // 🚨 해당 후기에 접수된 신고 횟수
	
	// AdminReviews.vue의 'targetName'을 담기 위한 필드 (DB 조회 시 JOIN 필요)
	private String targetName; 
}
