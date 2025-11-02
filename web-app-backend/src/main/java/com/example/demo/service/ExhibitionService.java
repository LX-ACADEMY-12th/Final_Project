package com.example.demo.service;

import com.example.demo.dto.ExhibitionDTO;
import com.example.demo.mapper.ExhibitionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 전시 '목록' 관련 비즈니스 로직을 처리하는 서비스
 */
@Service
public class ExhibitionService {

    // 1. ExhibitionMapper (목록용 매퍼)를 주입받습니다.
    // (ExhibitionDetailMapper 아님)
    @Autowired
    private ExhibitionMapper exhibitionMapper;

    /**
     * (카테고리별) 모든 전시 목록을 조회합니다.
     * Controller로부터 category 값을 받아서 Mapper로 그대로 전달합니다.
     */
    public List<ExhibitionDTO> findAllExhibitions(String category) {
        
        // 2. Mapper의 메서드를 호출해서 DB 결과를 받습니다.
        return exhibitionMapper.findAllExhibitions(category);
    }
}