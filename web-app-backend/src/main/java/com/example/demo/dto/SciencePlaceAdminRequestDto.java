package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SciencePlaceAdminRequestDto {

    @JsonProperty("place_name")
    private String placeName;

    @JsonProperty("address_detail")
    private String addressDetail;

    private String latitude;
    private String longitude;

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