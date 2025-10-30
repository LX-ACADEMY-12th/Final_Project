package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor // new TokenRefreshResponseDTO("token...") convenience
public class TokenRefreshResponseDTO {
    private String accessToken;
}