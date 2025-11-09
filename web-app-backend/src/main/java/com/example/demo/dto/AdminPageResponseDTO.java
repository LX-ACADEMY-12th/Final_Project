package com.example.demo.dto;

import java.util.List;

/**
 * 관리자 페이지 페이지네이션을 위한 DTO.
 * 현재 페이지 목록(content)과 전체 요소 개수(totalElements)를 포함합니다.
 */
public class AdminPageResponseDTO {

    private List<PlaceResultDTO> content;
    private int totalElements;

    // 생성자
    public AdminPageResponseDTO(List<PlaceResultDTO> content, int totalElements) {
        this.content = content;
        this.totalElements = totalElements;
    }

    // Getter 및 Setter
    public List<PlaceResultDTO> getContent() {
        return content;
    }

    public void setContent(List<PlaceResultDTO> content) {
        this.content = content;
    }

    public int getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(int totalElements) {
        this.totalElements = totalElements;
    }
}