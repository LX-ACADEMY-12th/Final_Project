package com.example.demo.service;

import com.example.demo.mapper.UserPathAnalysisMapper;
import com.example.demo.dto.UserPathSegmentDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserPathAnalysisService {

    private final UserPathAnalysisMapper userPathAnalysisMapper;

    public List<UserPathSegmentDTO> analyzePathSegments(LocalDate startDate, LocalDate endDate, String type) {
        if ("EXHIBITION".equalsIgnoreCase(type)) {
            // 전시관 내부 동선 분석 쿼리 호출
            return userPathAnalysisMapper.selectExhibitionPathSegmentsByPeriod(startDate, endDate);
        } else {
            // 장소 간 동선 분석 쿼리 호출 (기본값)
            return userPathAnalysisMapper.selectPlacePathSegmentsByPeriod(startDate, endDate);
        }
    }
}