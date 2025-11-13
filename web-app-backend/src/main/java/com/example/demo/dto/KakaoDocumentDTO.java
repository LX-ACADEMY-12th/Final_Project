package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoDocumentDTO {

    // [중요] 카카오는 위도(Latitude)를 'y', 경도(Longitude)를 'x'로 반환합니다.
    @JsonProperty("y") // JSON 필드 'y'를
    private BigDecimal latitude; // latitude 변수에 매핑

    @JsonProperty("x") // JSON 필드 'x'를
    private BigDecimal longitude; // longitude 변수에 매핑
}