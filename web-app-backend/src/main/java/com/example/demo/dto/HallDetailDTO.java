package com.example.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class HallDetailDTO {
    // LocationSection이 라우터 이동 시 사용할 ID
    private Long hallId;
    private String scienceCenterId;

    // 전시관 자체 정보
    private String exhibitionLocation;
    private List<String> floors;

    // 부모 과학관 정보
    private Double lat;
    private Double lng;
    private String operationalPeriod;
    private String operationHours;
    private String entranceFee;
}
