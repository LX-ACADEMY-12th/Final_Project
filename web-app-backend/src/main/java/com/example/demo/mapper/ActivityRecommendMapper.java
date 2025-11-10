package com.example.demo.mapper;

import com.example.demo.dto.ActivityRecommendDTO.PlaceDetailsForAI;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface ActivityRecommendMapper {

    /**
     * AI 프롬프트 생성을 위해 장소 이름과,
     * 해당 장소에 속한 모든 전시물(exhibit)의 이름을 조회합니다.
     *
     * @param placeId 장소 ID
     * @return 장소명과 전시물 이름 리스트
     */
    Optional<PlaceDetailsForAI> findPlaceDetailsById(Long placeId);
}