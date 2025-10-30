package com.example.demo.service;

import com.example.demo.dto.UserScheduleDTO;
import com.example.demo.mapper.ScheduleMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleMapper scheduleMapper;

    // 생성자 주입
    public ScheduleServiceImpl(ScheduleMapper scheduleMapper) {
        this.scheduleMapper = scheduleMapper;
    }

    @Override
    public List<UserScheduleDTO> getSchedulesByUserId(Long userId) {
        // MyBatis Mapper를 호출하여 데이터를 조회합니다.
        // 복잡한 계층형 데이터 조회 로직은 모두 Mapper.xml에서 처리됩니다.
        return scheduleMapper.findSchedulesByUserId(userId);
    }
}