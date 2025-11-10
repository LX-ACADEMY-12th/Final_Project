package com.example.demo.dto;

import lombok.Data;

@Data
public class HallSimpleResultDTO {
    private Long key;
    private String name;
    private String locationDescription;
    private String floors; // <-- String
}
