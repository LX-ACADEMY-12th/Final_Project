package com.example.demo.dto;
import lombok.Data;

@Data
public class AdminPlaceListDTO {
    private Long id; // place_id
    private String title; // place_name
    private String imageUrl; // main_image_url
    private String type; // place_type
    private String subject; // N:M (과학영역)
    private String grade; // N:M (학년)
    private String place; // address_detail
}