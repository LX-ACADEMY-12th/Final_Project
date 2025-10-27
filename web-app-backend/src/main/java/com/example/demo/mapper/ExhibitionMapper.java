package com.example.demo.mapper;

import com.example.demo.dto.CourseItemDTO; // [!!] DTO를 import
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExhibitionMapper {

    /**
     * [!!] Service가 DTO를 원하므로 반환 타입을 List<CourseItemDTO>로 변경
     */
    List<CourseItemDTO> findSimilarExhibitions(
            @Param("currentId") Long currentId,
            @Param("mainCategory") String mainCategory,
            @Param("grade") String grade
    );
}