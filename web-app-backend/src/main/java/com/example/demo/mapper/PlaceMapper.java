package com.example.demo.mapper;

import com.example.demo.dto.PlaceResultDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface PlaceMapper {
        // --- 전시 검색 ---
        // @Select(...) 제거
        List<PlaceResultDTO> findExhibitionsByFilter(@Param("subject") String subject, @Param("grade") String grade);

        // @Select(...) 제거
        List<PlaceResultDTO> findExhibitionsByRadius(@Param("subject") String subject, @Param("grade") String grade,
                                                     @Param("lat") double lat, @Param("lng") double lng, @Param("radiusMeters") double radiusMeters);

        // @Select(...) 제거
        List<PlaceResultDTO> findExhibitionsByRegion(@Param("subject") String subject, @Param("grade") String grade, @Param("regionPattern") String regionPattern);

        // --- 과학 장소(답사) 검색 ---
        // @Select(...) 제거
        List<PlaceResultDTO> findPlacesByFilter(@Param("subject") String subject, @Param("grade") String grade);

        // @Select(...) 제거
        List<PlaceResultDTO> findPlacesByRadius(@Param("subject") String subject, @Param("grade") String grade,
                                                @Param("lat") double lat, @Param("lng") double lng, @Param("radiusMeters") double radiusMeters);

        // @Select(...) 제거
        List<PlaceResultDTO> findPlacesByRegion(@Param("subject") String subject, @Param("grade") String grade, @Param("regionPattern") String regionPattern);
    }
