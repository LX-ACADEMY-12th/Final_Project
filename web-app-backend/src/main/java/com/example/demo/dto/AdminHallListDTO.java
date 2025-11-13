package com.example.demo.dto;
import lombok.Data;

@Data
public class AdminHallListDTO {
    private Long id; // hall_id
    private String name; // hall_name
    private String address; // address_detail
    private String imageUrl; // main_image_url
    private Long exhibitionCount; // 연결된 전시 개수
}