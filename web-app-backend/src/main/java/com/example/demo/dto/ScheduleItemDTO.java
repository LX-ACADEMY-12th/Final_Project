package com.example.demo.dto;

import lombok.Data;

import java.util.List;

/**
 * 개별 스케줄 아이템 DTO (JSON 요청 및 DB 전달용)
 */
@Data
public class ScheduleItemDTO {
    // 서비스 레이어에서 채워넣을 필드
    private Long scheduleId;

    // --- JSON 요청 필드 ---
    private Long itemId;
    private Long sourceItemId;
    private Integer sequence;
    private String itemType;
    private String customName;
    private String customAddress;
    private Double customLatitude;
    private Double customLongitude;

    // --- 스냅샷 필드 ---
    private String categoryName;
    private String gradeName;
    private List<String> subCategories;

}
