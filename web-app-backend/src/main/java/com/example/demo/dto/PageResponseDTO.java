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

    public PageResponseDTO(List<T> content, int pageNumber, int pageSize, long totalElements) {
        this.content = content;
        this.totalElements = totalElements;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        // totalPages는 Service가 아닌 DTO 내부에서 계산하여 일관성을 유지합니다.
        // Math.ceil()을 사용하여 총 개수를 페이지 크기로 나눈 후 올림하여 총 페이지 수를 계산합니다.
        this.totalPages = (int) Math.ceil((double) totalElements / pageSize);
    }
}
