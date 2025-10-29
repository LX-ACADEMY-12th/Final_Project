// UserController.java
package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.LoginRequestDTO;
import com.example.demo.dto.LoginResponseDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.service.UserService;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 1. 아이디 중복 확인 (GET /api/user/check-id/{loginId})
    @GetMapping("/check-id/{loginId}")
    public ResponseEntity<Boolean> checkIdDuplicate(@PathVariable String loginId) {
        // Service를 호출하여 중복 여부(true/false)를 반환합니다.
        boolean isDuplicate = userService.checkIdDuplicate(loginId);
        return ResponseEntity.ok(isDuplicate); 
    }

    // 2. 이메일 중복 확인 (GET /api/user/check-email/{email})
    @GetMapping("/check-email/{email}")
    public ResponseEntity<Boolean> checkEmailDuplicate(@PathVariable String email) {
        // Service를 호출하여 중복 여부(true/false)를 반환합니다.
        boolean isDuplicate = userService.checkEmailDuplicate(email);
        return ResponseEntity.ok(isDuplicate);
    }

    // 3. 회원가입 (POST /api/user/signup)
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody UserDTO userDTO) {
        // 아이디나 이메일 중복 체크를 다시 수행하는 것이 좋으나, 여기서는 간단히 로직만 호출합니다.
        if (userService.checkIdDuplicate(userDTO.getLoginId()) || userService.checkEmailDuplicate(userDTO.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("아이디 또는 이메일이 이미 사용 중입니다.");
        }
        
        try {
            userService.registerUser(userDTO);
            // HTTP 201 Created 응답 (자원 생성 성공)
            return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 성공"); 
        } catch (Exception e) {
            // DB 오류 등의 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원가입 처리 중 오류가 발생했습니다.");
        }
    }

    // 4. 로그인 (POST /api/user/login)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequest) {
        LoginResponseDTO responseDTO = userService.login(loginRequest);

        if (responseDTO == null) {
            // 인증 실패 (아이디 또는 비밀번호 불일치)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디 또는 비밀번호를 확인해주세요.");
        }
        
        // 인증 성공 (HTTP 200 OK)
        return ResponseEntity.ok(responseDTO);
    }
    
    // 5. 로그인 아이디로 사용자 상세 정보 조회 (GET /api/user/info)
    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String token) {
        // 토큰에서 loginId 추출 (모의 토큰 파싱)
        String loginId = parseLoginIdFromToken(token);
        if (loginId == null) {
            // 토큰 형식이 잘못되었거나 'Bearer ' 접두사가 없는 경우
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }

        UserDTO userDTO = userService.getUserInfoByLoginId(loginId);
        if (userDTO == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자 정보를 찾을 수 없습니다.");
        }
        
        // 민감 정보(비밀번호) 제거 후 반환
        userDTO.setPassword(null);
        return ResponseEntity.ok(userDTO);
    }

    // 6. 회원탈퇴 (DELETE /api/user/withdraw) (⭐추가됨⭐)
    @DeleteMapping("/withdraw")
    public ResponseEntity<?> withdrawUser(@RequestHeader("Authorization") String token) {
        // 1. 토큰에서 loginId 추출 (인증된 사용자 확인)
        String loginId = parseLoginIdFromToken(token);
        if (loginId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }

        try {
            // 2. Service 계층 호출: 실제 회원탈퇴(DB 삭제) 로직 실행
            userService.withdrawUser(loginId);
            
            // 3. 성공 응답 (HTTP 204 No Content: 성공적으로 삭제되었으나 본문에 내용 없음)
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            // 삭제 중 DB 오류 등 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원 탈퇴 처리 중 오류가 발생했습니다.");
        }
    }
    
    // 7. 사용자 정보 수정 (PUT /api/user/update) (⭐새로 추가됨⭐)
    @PutMapping("/update")
    public ResponseEntity<?> updateUserInfo(
        @RequestHeader("Authorization") String token, 
        @RequestBody UserDTO userDTO) {

        // 1. 토큰에서 loginId 추출 (사용자 인증)
        String loginId = parseLoginIdFromToken(token);
        if (loginId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }
        
        // DTO에 인증된 loginId를 설정 (사용자가 보내지 않았을 경우를 대비)
        userDTO.setLoginId(loginId); 

        try {
            // 2. Service 호출하여 정보 업데이트
            int updatedRows = userService.updateUserInfo(userDTO);

            if (updatedRows > 0) {
                // HTTP 200 OK
                return ResponseEntity.ok("사용자 정보가 성공적으로 수정되었습니다.");
            } else {
                // 업데이트할 사용자 정보를 찾지 못한 경우
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("수정할 사용자 정보를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사용자 정보 수정 중 오류가 발생했습니다.");
        }
    }

    // 8. 모의 토큰에서 loginId를 추출하는 메서드
    private String parseLoginIdFromToken(String token) {
        // 토큰은 "Bearer loginId_timestamp" 형태라고 가정합니다.
        if (token == null || !token.startsWith("Bearer ")) {
            return null;
        }
        // "Bearer " 접두사 제거
        String actualToken = token.substring(7); 
        
        // "_"(언더바)를 기준으로 loginId를 추출
        int separatorIndex = actualToken.indexOf('_');
        if (separatorIndex > 0) {
            return actualToken.substring(0, separatorIndex);
        }
        return null; // 형식 불일치
    }
}