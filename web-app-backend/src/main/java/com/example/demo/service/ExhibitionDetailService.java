package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.ExhibitionDetailDTO;
import com.example.demo.mapper.ExhibitionDetailMapper;

//1. "@Service" 어노테이션
//Spring Boot에게 이 클래스가 '서비스 계층'의 컴포넌트(Bean)임을 알려줍니다.
//Spring이 시작할 때 이 클래스의 인스턴스를 자동으로 생성해서 관리합니다.
@Service
public class ExhibitionDetailService {

	// 2. "@Autowired" (의존성 주입)
    // Spring에게 "아까 @Mapper로 만든 ExhibitionMapper 인터페이스의
    // 구현체(Bean)를 찾아서 여기에 자동으로 연결(주입)해 줘" 라고 요청합니다.
	@Autowired
	private ExhibitionDetailMapper exhibitionDetailMapper;
	
	/**
     * 컨트롤러(Controller)가 호출할 메서드입니다.
     * 전시 ID를 받아서 상세 정보를 반환합니다.
     */
	public ExhibitionDetailDTO getfindExhibitionDetails(Long exhibitionId) {
		
		// 3. Mapper 메서드 호출
        // 실제 DB 작업은 Mapper에게 시킵니다.
        // exhibitionMapper.findExhibitionById(exhibitionId)가
        // ExhibitionMapper.xml의 SQL을 실행하고
        // 그 결과를 ExhibitionDetailDTO 객체로 반환해 줍니다.
		ExhibitionDetailDTO dto = exhibitionDetailMapper.findExhibitionById(exhibitionId);
		
		// 4. (선택) 비즈니스 로직
        // 만약 DB에서 가져온 데이터로 추가적인 처리가 필요하면 여기서 합니다.
        // 예: if (dto == null) {
        //         // 또는 throw new CustomException("해당 전시를 찾을 수 없습니다.");
        //         return null; 
        //     }
        // 예: dto.setExhibitionName(dto.getExhibitionName() + " [특별 할인 중]");
		
		//5. 컨트롤러에게 결과 반환
		return dto;
	}
}
