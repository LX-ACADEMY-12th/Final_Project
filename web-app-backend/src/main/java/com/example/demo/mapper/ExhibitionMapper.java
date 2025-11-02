package com.example.demo.mapper;


import com.example.demo.dto.LikedCourseListDTO;
import com.example.demo.dto.ExhibitionDTO; // [!!] 1. '목록용 DTO' import 추가
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExhibitionMapper {

    // ID 목록으로 전시 정보 리스트 조회 (목록 카드용)
    List<LikedCourseListDTO> findExhibitionsForLikedList(@Param("ids") List<Long> ids);


    List<ExhibitionDTO> findAllExhibitions(@Param("category") String category);

}