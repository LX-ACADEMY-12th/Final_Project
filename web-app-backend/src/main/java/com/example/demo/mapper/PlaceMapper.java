package com.example.demo.mapper;

import com.example.demo.dto.CourseItemDTO;
import com.example.demo.dto.LikedCourseListDTO;
import com.example.demo.dto.PlaceResultDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PlaceMapper {
        // --- 전시 검색 ---
        // 일반 필터 검색
        List<PlaceResultDTO> findExhibitionsByFilter(@Param("subject") String subject, @Param("grade") String grade);
        // 반경 필터 검색
        List<PlaceResultDTO> findExhibitionsByRadius(@Param("subject") String subject, @Param("grade") String grade,
                                                     @Param("lat") double lat, @Param("lng") double lng, @Param("radius") double radius);

        List<PlaceResultDTO> findExhibitionsByRegion(@Param("subject") String subject, @Param("grade") String grade, @Param("regionPattern") String regionPattern);
        // 추천 전시 검색
        List<CourseItemDTO> findSimilarExhibition(
                @Param("currentId") Long currentId,
                @Param("mainCategory") String mainCategory,
                @Param("grade") String grade);

        // --- 과학 장소(답사) 검색 ---
        // 일반 필터 검색
        List<PlaceResultDTO> findPlacesByFilter(@Param("subject") String subject, @Param("grade") String grade);
        // 반경 필터 검색
        List<PlaceResultDTO> findPlacesByRadius(@Param("subject") String subject, @Param("grade") String grade,
                                                @Param("lat") double lat, @Param("lng") double lng, @Param("radius") double radius);

        List<PlaceResultDTO> findPlacesByRegion(@Param("subject") String subject, @Param("grade") String grade, @Param("regionPattern") String regionPattern);

        // 추천 장소 검색
        List<CourseItemDTO> findSimilarSciencePlace(
                @Param("currentId") Long currentId,
                @Param("mainCategory") String mainCategory,
                @Param("grade") String grade
        );

        // ID 목록으로 장소 정보 리스트 조회 (목록 카드용)
        List<LikedCourseListDTO> findPlacesForLikedList(@Param("ids") List<Long> ids);
}
