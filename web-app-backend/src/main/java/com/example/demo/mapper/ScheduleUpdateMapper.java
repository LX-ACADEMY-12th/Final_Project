package com.example.demo.mapper;

import com.example.demo.dto.ScheduleItemDTO;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ScheduleUpdateMapper {

    void deleteItemsByScheduleId(Long scheduleId);

    void insertScheduleItems(List<ScheduleItemDTO> items);
}