package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoCoord2AddressResponseDTO {
    private List<KakaoCoord2AddressDTO> documents;
}