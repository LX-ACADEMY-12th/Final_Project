package com.example.demo.mapper;

import com.example.demo.dto.CourseItemDTO; // [!!] DTO를 import
import com.example.demo.dto.LikedCourseListDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExhibitionMapper {

    // ID 목록으로 전시 정보 리스트 조회 (목록 카드용)
    List<LikedCourseListDTO> findExhibitionsForLikedList(@Param("ids") List<Long> ids);

}