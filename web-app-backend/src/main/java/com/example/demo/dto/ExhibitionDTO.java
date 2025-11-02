package com.example.demo.dto;

import lombok.Data;

@Data
public class ExhibitionDTO {
    private Long exhibitionId;
    private String exhibitionName;
    private String description;
    
    // DB의 'image_file_name' 컬럼과 매칭될 필드
    private String imageFileName; 
}