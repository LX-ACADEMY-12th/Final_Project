package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoCoord2AddressDTO {
    // 도로명 주소
    @JsonProperty("road_address")
    private Map<String, String> roadAddress;

    // 지번 주소
    @JsonProperty("address")
    private Map<String, String> jibunAddress;

    // 도로명 주소가 있으면 도로명, 없으면 지번 주소를 반환
    public String getAddressName() {
        if (this.roadAddress != null && this.roadAddress.get("address_name") != null) {
            return this.roadAddress.get("address_name");
        }
        if (this.jibunAddress != null && this.jibunAddress.get("address_name") != null) {
            return this.jibunAddress.get("address_name");
        }
        return null;
    }
}