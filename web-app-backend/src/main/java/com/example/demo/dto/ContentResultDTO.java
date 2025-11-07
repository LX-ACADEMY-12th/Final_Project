package com.example.demo.dto;

import lombok.Data;
import java.util.List;

@Data
public class ContentResultDTO {
    // <id property="id" column="contentId"/>
    private Long id;

    // <result property="imageUrl" column="imageUrl"/>
    private String imageUrl;

    // <result property="title" column="title"/>
    private String title;

    // <result property="exhibitionLists" column="exhibitionLists" ... />
    // (쿼리에서 ARRAY_AGG(...) AS exhibitionLists)
    private List<String> exhibitionLists;

    // <result property="subject" column="categoryName" ... />
    // (쿼리에서 ARRAY_AGG(...) AS categoryName)
    private String subject;

    // <result property="hashtags" column="subCategoryName" ... />
    // (쿼리에서 ARRAY_AGG(...) AS subCategoryName)
    private List<String> hashtags;

    // <result property="type" column="type"/>
    private String type;

    // <result property="place" column="place"/>
    private String place;

    // <result property="grade" column="grade"/>
    private String grade;

    // <result property="lat" column="lat"/>
    private Double lat;

    // <result property="lng" column="lng"/>
    private Double lng;

    // "exhibition" 또는 "science_place"
    private String itemType;
}
