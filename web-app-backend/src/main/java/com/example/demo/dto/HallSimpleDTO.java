package com.example.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class HallSimpleDTO {

    private Long key;
    private String name;
    private String locationDescription;
    private List<String> floors;
}
