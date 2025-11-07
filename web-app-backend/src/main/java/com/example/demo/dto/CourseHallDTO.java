package com.example.demo.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CourseHallDTO {
    private Long hallId;
    private String hallName;
    private int relevantExhibitionCount;
    private List<String> subCategories;
    private List<String> relevantExhibitions;
    private String combinedDescriptions;
    private Double distanceMeters;
}
