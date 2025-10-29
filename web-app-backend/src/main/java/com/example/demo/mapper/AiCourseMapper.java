package com.example.demo.mapper;

import com.example.demo.dto.AiRecommendedCourseDTO;
import com.example.demo.service.RecommendedCourseService.AiCourseItemDto; // 내부 DTO 사용
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

import java.util.List;

@Mapper
public interface AiCourseMapper {

    /**
     * ai_recommended_course 테이블에 레코드를 삽입하고,
     * 전달된 DTO의 aiCourseId 필드에 생성된 키를 채웁니다.
     * @param course 코스 상세 정보 DTO. aiCourseId가 설정됩니다.
     * @return 영향을 받은 행 수 (보통 1).
     */
    @Options(useGeneratedKeys = true, keyProperty = "aiCourseId", keyColumn = "ai_course_id")
    int insertAiRecommendedCourse(AiRecommendedCourseDTO course);

    /**
     * ai_course_item 테이블에 아이템 목록을 배치 삽입합니다.
     * @param items 삽입할 아이템 목록.
     */
    void insertAiCourseItems(List<AiCourseItemDto> items);
}