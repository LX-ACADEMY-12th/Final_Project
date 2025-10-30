package com.example.demo.mapper;

import com.example.demo.dto.ScheduleItemDetailDTO;
import com.example.demo.dto.UserScheduleDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ScheduleMapper {

    /**
     * 사용자 ID로 모든 final_schedule과 하위 item 리스트를 조회 (1+N 방지)
     */
    List<UserScheduleDTO> findSchedulesByUserId(@Param("userId") Long userId);

    /**
     * (위 쿼리에서 내부적으로 사용) 특정 schedule_id에 속한 상세 아이템 리스트 조회
     */
    List<ScheduleItemDetailDTO> findScheduleItemsByScheduleId(@Param("scheduleId") Long scheduleId);
}