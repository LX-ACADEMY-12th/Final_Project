package com.example.demo.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor //모든 필드 값을 파라미터로 받는 생성자를 생성
@Data
@NoArgsConstructor // 파라미터가 없는 디폴트 생성자를 생성
public class PageResponseDTO<T> {
	private List<T> content;      // 현재 페이지의 데이터 리스트
    private int totalPages;     // 총 페이지 수
    private long totalElements; // 총 데이터(리뷰) 수
    private int pageNumber;     // 현재 페이지 번호 (0부터 시작 or 1부터 시작)
    private int pageSize;       // 페이지 당 데이터 수
}
