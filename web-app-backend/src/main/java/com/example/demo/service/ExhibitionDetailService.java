package com.example.demo.service;

import java.util.HashMap;
import java.util.Map;

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
     * 전시관 ID를 받아서 상세 정보를 반환합니다.
     */
	public ExhibitionDetailDTO getfindExhibitionDetails(Long exhibitionId,String mainCategoryTags, String subCategoryTags,String gradeTags) {
		
		// 파라미터를 담을 Map 생성
		Map<String, Object> params = new HashMap<>();
		
		// 2. Map에 파라미터를 "Key", "Value" 쌍으로 담습니다.
        // 이 "Key" 이름(문자열)이 Mapper.xml의 #{...} 이름과 일치해야 합니다.
		// 메인 / 서브 / 학년
		params.put("exhibitionHallId", exhibitionId);
		params.put("mainCategoryTags", mainCategoryTags);
		params.put("subCategoryTags", subCategoryTags);
		params.put("gradeTags", gradeTags);

		// 3. Mapper 메서드 호출 시, 3개의 값이 아닌 'Map' 객체 1개를 전달합니다.
		ExhibitionDetailDTO dto = exhibitionDetailMapper.findExhibitionById(params);

		// 4. 컨트롤러에게 결과 반환
		return dto;
	}
}
