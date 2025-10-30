package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.mapper.AiCourseMapper; // 매퍼 이름 가정
import com.example.demo.mapper.FinalScheduleMapper; // 매퍼 이름 가정
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 트랜잭션 임포트

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendedCourseService {

    private final AiCourseMapper aiCourseMapper;
    private final FinalScheduleMapper finalScheduleMapper;

    @Transactional // 모든 DB 작업이 함께 성공하거나 실패하도록 보장
    public void saveRecommendedCourse(RecommendedCourseSaveRequestDTO requestDto, Long userId) {
        log.info("[TX] 추천 코스 저장 트랜잭션 시작 - userId: {}", userId);

        if (requestDto.getItems() == null || requestDto.getItems().isEmpty()) {
            log.warn("[TX] 요청에 아이템 없음 - userId: {}. 저장 중단.", userId);
            throw new IllegalArgumentException("추천 코스에 포함된 항목이 없습니다.");
        }

        try {
            // --- 1단계 & 2단계: ai_recommended_course 테이블과 ai_course_item 테이블에 저장 ---
            log.info("[TX] AI 추천 기록 저장 준비.");
            AiRecommendedCourseDTO aiCourseDto = new AiRecommendedCourseDTO(
                    requestDto.getScheduleName(),
                    requestDto.getSourceId(),
                    requestDto.getSourceCourseType()
            );
            log.info("[TX] 들어온 AI 추천 코스 정보 {}", aiCourseDto);

            aiCourseMapper.insertAiRecommendedCourse(aiCourseDto);

            Long generatedAiCourseId = aiCourseDto.getAiCourseId(); // 생성된 ID 가져오기

            if (generatedAiCourseId == null) {
                throw new RuntimeException("생성된 ai_course_id를 가져오지 못했습니다.");
            }

            log.info("[TX] ai_recommended_course 저장 완료 - ID: {}", generatedAiCourseId);

            // ai_course_item 배치 삽입 준비
            final Long finalAiCourseId = generatedAiCourseId; // 람다에서 사용하기 위해 final 변수 필요
            List<AiCourseItemDto> aiCourseItems = requestDto.getItems().stream()
                    .map(item -> new AiCourseItemDto(finalAiCourseId, item.getExhibitionId(), item.getPlaceId(), item.getSequence()))
                    .collect(Collectors.toList());
            log.info("[TX] 들어온 ai_courese_item {}", aiCourseItems);

            if (!aiCourseItems.isEmpty()) {
                aiCourseMapper.insertAiCourseItems(aiCourseItems);
                log.info("[TX] ai_course_item에 {}개 아이템 저장 완료 - aiCourseId: {}", aiCourseItems.size(), generatedAiCourseId);
            }

            // --- 3단계 & 4단계: final_schedule 와 final_schedule_item 에 저장 ---
            log.debug("[TX] 사용자 최종 스케줄 저장 준비.");
            FinalScheduleDTO finalScheduleDto = new FinalScheduleDTO(
                    userId,
                    requestDto.getScheduleName(),
                    requestDto.getSourceCourseType(),
                    finalAiCourseId
            );
            log.info("[TX] 들어온 final 스케줄 테이블_item {}", finalScheduleDto);
            finalScheduleMapper.insertFinalSchedule(finalScheduleDto);
            Long generatedScheduleId = finalScheduleDto.getScheduleId(); // 생성된 ID 가져오기

            if (generatedScheduleId == null) {
                throw new RuntimeException("생성된 schedule_id를 가져오지 못했습니다.");
            }
            log.info("[TX] final_schedule 저장 완료 - ID: {}", generatedScheduleId);

            // final_schedule_item 배치 삽입 준비
            final Long finalScheduleId = generatedScheduleId; // 람다에서 사용하기 위해 final 변수 필요
            List<FinalScheduleItemDto> finalScheduleItems = requestDto.getItems().stream()
                    .map(item -> new FinalScheduleItemDto(
                            finalScheduleId,
                            item.getExhibitionId(),
                            item.getPlaceId(),
                            item.getSequence()
                    ))
                    .collect(Collectors.toList());

            if (!finalScheduleItems.isEmpty()) {
                finalScheduleMapper.insertFinalScheduleItems(finalScheduleItems);
                log.info("[TX] final_schedule_item에 {}개 아이템 저장 완료 - scheduleId: {}", finalScheduleItems.size(), generatedScheduleId);
            }

            log.info("[TX] 트랜잭션 커밋 성공 - userId: {}", requestDto.getUserId());

        } catch (Exception e) {
            log.error("[TX] 트랜잭션 실패 - userId: {}. 롤백 실행.", requestDto.getUserId(), e);
            // Spring의 @Transactional이 롤백을 수행하도록 예외를 다시 던짐
            throw new RuntimeException("추천 코스 저장 실패, 트랜잭션이 롤백되었습니다.", e);
        }
    }

    // --- 매퍼 배치 삽입용 헬퍼 DTO ---
    // 내부 클래스 또는 별도 파일로 정의 가능
    @Data
    @NoArgsConstructor
    public static class AiCourseItemDto {
        private Long aiCourseId; // finalAiCourseId
        private Long exhibitionId; // exhibitionId에서 매핑됨
        private Long placeId; // placeId에서 매핑됨
        private Integer sequence;
        public AiCourseItemDto(Long aiCourseId, Long exhibitionId, Long placeId, Integer sequence) {
            this.aiCourseId = aiCourseId; this.exhibitionId = exhibitionId; this.placeId = placeId; this.sequence = sequence;
        }
    }

    @Data @NoArgsConstructor
    public static class FinalScheduleItemDto {
        private Long scheduleId;
        private Long exhibitionId;
        private Long placeId;
        private Integer sequence;
//        private Long itemId1; // targetId에서 매핑됨
        // 추천 아이템의 경우 custom 필드는 기본적으로 null
        public FinalScheduleItemDto(Long scheduleId, Long exhibitionId, Long placeId, Integer sequence) {
            this.scheduleId = scheduleId; this.exhibitionId=exhibitionId; this.placeId=placeId; this.sequence = sequence;
        }
    }
}