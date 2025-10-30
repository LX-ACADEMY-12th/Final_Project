package com.example.demo.service;

import com.example.demo.dto.ScheduleItemDTO;
import com.example.demo.dto.ScheduleSaveRequestDTO;
import com.example.demo.mapper.ScheduleUpdateMapper;
import lombok.RequiredArgsConstructor; // Lombok 임포트
import lombok.extern.slf4j.Slf4j; // Slf4j (로깅) 임포트
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j // 로거 자동 생성
@Service
@RequiredArgsConstructor // final 매퍼 자동 주입
public class ScheduleUpdateService {

    private final ScheduleUpdateMapper scheduleUpdateMapper;

    @Transactional
    public void saveScheduleItems(ScheduleSaveRequestDTO requestDto) {
        Long scheduleId = requestDto.getScheduleId();
        List<ScheduleItemDTO> items = requestDto.getItems();

        log.info("[TX] Starting transaction for saving items, scheduleId: {}", scheduleId);

        try {
            // 1. 기존 아이템 모두 삭제
            log.info("[TX] Deleting old items for scheduleId: {}", scheduleId);
            scheduleUpdateMapper.deleteItemsByScheduleId(scheduleId);

            // 2. 새 아이템 목록이 비어있지 않다면 삽입
            if (items != null && !items.isEmpty()) {

                log.info("[TX] Inserting {} new items for scheduleId: {}", items.size(), scheduleId);

                // 각 아이템 DTO에 부모 scheduleId 설정
                for (ScheduleItemDTO item : items) {
                    item.setScheduleId(scheduleId);
                }

                // Batch Insert 호출
                scheduleUpdateMapper.insertScheduleItems(items);
            } else {
                log.info("[TX] No items to insert for scheduleId: {}. Only deletion was performed.", scheduleId);
            }

            log.info("[TX] Successfully committed transaction for scheduleId: {}", scheduleId);

        } catch (Exception e) {
            // @Transactional에 의해 이 예외가 발생하면 롤백됩니다.
            log.error("[TX] Failed transaction for scheduleId: {}. Rolling back.", scheduleId, e);
            // 예외를 다시 던져서 @Transactional이 롤백을 수행하도록 합니다.
            throw new RuntimeException("Failed to save schedule items, transaction rolled back.", e);
        }
    }
}