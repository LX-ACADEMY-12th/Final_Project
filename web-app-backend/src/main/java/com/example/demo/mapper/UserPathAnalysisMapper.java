package com.example.demo.mapper;

import com.example.demo.dto.UserPathSegmentDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface UserPathAnalysisMapper {
    // 💡 메서드 이름 변경 및 분리
    List<UserPathSegmentDTO> selectPlacePathSegmentsByPeriod(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // 💡 전시관 내부 동선 분석용 메서드
    List<UserPathSegmentDTO> selectExhibitionPathSegmentsByPeriod(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}