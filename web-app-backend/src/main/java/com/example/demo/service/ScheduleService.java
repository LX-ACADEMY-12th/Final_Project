package com.example.demo.service;

import com.example.demo.dto.UserScheduleDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ScheduleService {
    /**
     * 사용자 ID로 모든 최종 일정 및 상세 아이템 목록을 조회합니다.
     * @param userId 사용자 ID
     * @return 계층 구조의 스케줄 DTO 리스트
     */
    List<UserScheduleDTO> getSchedulesByUserId(Long userId);
}