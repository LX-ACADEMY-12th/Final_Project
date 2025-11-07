package com.example.demo.mapper;

import com.example.demo.dto.CourseHallDTO;
import com.example.demo.dto.CourseItemDTO;
import com.example.demo.dto.LikedCourseListDTO;
import com.example.demo.dto.ContentResultDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ContentMapper {
        // --- 콘텐츠 검색 ---
        // 일반 필터 검색
        List<ContentResultDTO> findContentByFilter(@Param("subject") String subject, @Param("grade") String grade);
        // 반경 필터 검색
        List<ContentResultDTO> findContentByRadius(@Param("subject") String subject, @Param("grade") String grade,
                                                       @Param("lat") double lat, @Param("lng") double lng, @Param("radius") double radius);
        // 지역 필터 검색
        List<ContentResultDTO> findContentByRegion(@Param("subject") String subject, @Param("grade") String grade, @Param("regionPattern") String regionPattern);

        // 추천 전시관 검색
        List<CourseHallDTO> findSimilarExhibition(
                @Param("currentId") Long currentId,
                @Param("mainCategory") String mainCategory,
                @Param("grade") String grade);
        // 전시관 id로 정보 가져오기 -> 추천 아이템으로 사용할거임.
        List<CourseItemDTO> findExhibitionsByHallIdAndCriteria (
                @Param("hallId") Long hallId,
                @Param("mainCategory") String mainCategory,
                @Param("grade") String grade
        );
        
        // 추천 장소 검색
        List<CourseItemDTO> findSimilarSciencePlace(
                @Param("currentId") Long currentId,
                @Param("mainCategory") String mainCategory,
                @Param("grade") String grade
        );

        // ID 목록으로 장소 정보 리스트 조회 (목록 카드용)
        List<LikedCourseListDTO> findPlacesForLikedList(@Param("ids") List<Long> ids);
}
