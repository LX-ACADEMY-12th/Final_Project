package com.example.demo.dto;

import lombok.Data;

@Data
public class HallDetailResultDTO {
    // DB의 JSON 문자열을 그대로 받기 위해 String으로 선언
    private Long hallId;
    private String scienceCenterId;
    private String exhibitionLocation;
    private String floors; // <-- String
    private Double lat;
    private Double lng;
    private String operationPeriod;
    private String operationHours;
    private String entranceFee;
}
