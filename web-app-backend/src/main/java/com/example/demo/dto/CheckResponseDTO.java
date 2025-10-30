package com.example.demo.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter // Setter가 필요 없다면 이 줄을 삭제해도 됩니다.
@RequiredArgsConstructor // final 필드가 없으므로, 기본적으로 모든 필드를 포함하는 생성자가 생성됨
public class CheckResponseDTO {
    
    // 중복 여부 (true: 중복됨, false: 중복되지 않음/사용 가능)
    private final boolean duplicate;

    // 메시지 (선택 사항: 프론트엔드에 추가 정보를 전달할 때 사용)
    private final String message;
    
    // 필드에 final 키워드를 추가하여 불변성을 확보
    // ⭐ @RequiredArgsConstructor가 final 필드 2개를 인자로 받는 생성자를 자동으로 생성합니다. ⭐
    // ⭐ @Getter가 public boolean isDuplicate()와 public String getMessage()를 자동으로 생성합니다. ⭐
    // ⭐ (원래 코드에서 Setter가 있었기 때문에 @Setter도 남겨두었지만, 응답 객체에서는 보통 제거합니다.) ⭐
}