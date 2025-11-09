package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ExhibitionAdminRequestDto {

    @JsonProperty("exhibition_name")
    private String exhibitionName;

    @JsonProperty("hall_name")
    private String hallName;

    @JsonProperty("address_detail")
    private String addressDetail;

    private String latitude;
    private String longitude;

    @JsonProperty("start_date")
    private String startDate; // (DB 타입이 Date라면 Service에서 변환)

    @JsonProperty("end_date")
    private String endDate;

    @JsonProperty("admission_fee")
    private String admissionFee;

    @JsonProperty("opening_hours")
    private String openingHours;

    private String description;
    private String grade;
    private String subject;
    private String type;
    
    // 이 필드는 Service에서 채워줍니다.
    private String mainImageUrl;
}