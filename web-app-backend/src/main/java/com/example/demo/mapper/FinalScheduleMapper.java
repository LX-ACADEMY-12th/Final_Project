package com.example.demo.mapper;

import com.example.demo.dto.FinalScheduleDTO;
import com.example.demo.service.RecommendedCourseService.FinalScheduleItemDto; // 내부 DTO 사용
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

import java.util.List;

@Mapper
public interface FinalScheduleMapper {

    /**
     * final_schedule 테이블에 레코드를 삽입하고,
     * 전달된 DTO의 scheduleId 필드에 생성된 키를 채웁니다.
     * @param schedule 스케줄 상세 정보 DTO. scheduleId가 설정됩니다.
     * @return 영향을 받은 행 수 (보통 1).
     */
    @Options(useGeneratedKeys = true, keyProperty = "scheduleId", keyColumn = "schedule_id")
    int insertFinalSchedule(FinalScheduleDTO schedule);

    /**
     * final_schedule_item 테이블에 아이템 목록을 배치 삽입합니다.
     * @param items 삽입할 아이템 목록.
     */
    void insertFinalScheduleItems(List<FinalScheduleItemDto> items);

    // --- 이전 대화에서 이미 존재할 수 있는 메소드들 ---
    // void deleteItemsByScheduleId(Long scheduleId);
    // void insertScheduleItems(List<com.example.demo.dto.ScheduleItemDto> items); // 주의: 파라미터 타입 다름
}