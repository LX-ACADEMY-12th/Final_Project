package com.example.demo.dto;

import lombok.Data;
import java.util.List;

@Data
public class ScheduleSaveRequestDTO {

    private Long scheduleId;
    private List<ScheduleItemDTO> items;
}
