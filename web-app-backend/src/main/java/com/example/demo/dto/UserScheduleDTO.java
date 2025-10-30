package com.example.demo.dto;

import java.util.List;
import lombok.Data;

@Data
public class UserScheduleDTO {
    private Long scheduleId;
    private String scheduleName;
    private String sourceCourseType;
    private Long sourceAiCourseId;
    private Long sourceInnerCourseId;

    // 이 스케줄에 속한 상세 아이템 목록
    private List<ScheduleItemDetailDTO> items;
}
