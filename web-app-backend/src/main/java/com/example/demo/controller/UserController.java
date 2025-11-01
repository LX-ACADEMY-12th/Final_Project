package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
// 🟢 Spring Security의 Authentication
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.LoginRequestDTO;
import com.example.demo.dto.LoginResponseDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.service.UserService;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap; // 🟢
import java.util.Map; // 🟢

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 1. 아이디 중복 확인 (변경 없음)
    @GetMapping("/check-id/{loginId}")
    public ResponseEntity<Boolean> checkIdDuplicate(@PathVariable String loginId) {
        boolean isDuplicate = userService.checkIdDuplicate(loginId);
        return ResponseEntity.ok(isDuplicate);
    }

    // 2. 이메일 중복 확인 (변경 없음)
    @GetMapping("/check-email/{email}")
    public ResponseEntity<Boolean> checkEmailDuplicate(@PathVariable String email) {
        boolean isDuplicate = userService.checkEmailDuplicate(email);
        return ResponseEntity.ok(isDuplicate);
    }

    // 3. 회원가입 (변경 없음 - UserService에서 암호화)
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody UserDTO userDTO) {
        if (userService.checkIdDuplicate(userDTO.getLoginId()) || userService.checkEmailDuplicate(userDTO.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("아이디 또는 이메일이 이미 사용 중입니다.");
        }

        try {
            userService.registerUser(userDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 성공");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원가입 처리 중 오류가 발생했습니다.");
        }
    }

    // 4. 로그인 (변경 없음 - UserService에서 토큰 발급)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequest) {
        LoginResponseDTO responseDTO = userService.login(loginRequest);

        if (responseDTO == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디 또는 비밀번호를 확인해주세요.");
        }

        return ResponseEntity.ok(responseDTO);
    }

    // 5. 🟢 사용자 상세 정보 조회
    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(Authentication authentication) { // 🟢 [추가]

        // 🟢 토큰에서 'userId' 추출
        Long userId = getUserIdFromAuthentication(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }

        // 🟢 Service 계층에 userId 사용
        UserDTO userDTO = userService.getUserInfoByUserId(userId); // (Mapper에 selectUserById 필요)
        if (userDTO == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자 정보를 찾을 수 없습니다.");
        }

        userDTO.setPassword(null); // 민감 정보 제거
        return ResponseEntity.ok(userDTO);
    }

    /**
     * 🟢 만료된 프로필 이미지 URL 갱신 API
     * 앱 로드 시 호출되어 localStorage의 만료된 Signed URL을 새로고침합니다.
     */
    @GetMapping("/profile-url")
    public ResponseEntity<?> getRefreshedProfileUrl(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }

        try {
            // 1. UserService에 새 메서드 호출
            String signedUrl = userService.getRefreshedProfileUrl(userId);

            // 2. { "url": "https://..." } 형식의 간단한 JSON으로 반환
            Map<String, String> response = new HashMap<>();
            response.put("url", signedUrl);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // UserDTO가 null이거나, blobName이 null일 경우
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("프로필 이미지 정보를 찾을 수 없습니다.");
        }
    }

    // 6. 🟢 회원탈퇴
    @DeleteMapping("/withdraw")
    public ResponseEntity<?> withdrawUser(Authentication authentication) { // 🟢 [추가]

        Long userId = getUserIdFromAuthentication(authentication); // 🟢 [수정]
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }

        try {
            userService.withdrawUserById(userId); // 🟢 [수정] (Mapper에 deleteUserById 필요)
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원 탈퇴 처리 중 오류가 발생했습니다.");
        }
    }

    // 7. 🟢 사용자 정보 수정
    @PutMapping("/update")
    public ResponseEntity<?> updateUserInfo(
            Authentication authentication, // 🟢 [추가]
            @RequestPart("dto") UserDTO userDTO,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
            ) {
    
        // 토큰에서 userId 꺼내기
        Long userId = getUserIdFromAuthentication(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }

        // 🟢 DTO에 인증된 사용자의 userId를 강제로 설정 (보안)
        userDTO.setUserId(userId.intValue());

        try {
            // 🟢 Service가 GCS 업로드 후 '업데이트된 DTO'를 반환하도록 수정
            UserDTO updatedUser = userService.updateUserInfo(userDTO, profileImage);

            if (updatedUser != null) {
                // 🟢 성공 문자열 대신 '업데이트된 DTO'를 반환
                return ResponseEntity.ok(updatedUser);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("수정할 사용자 정보를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("사용자 정보 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 🟢 [추가] Authentication 객체에서 'Long userId'를 추출하는 헬퍼 메서드
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            // JwtTokenProvider가 UserDetails의 username에 userId(문자열)를 저장했음
            String userIdString = ((UserDetails) principal).getUsername();
            try {
                return Long.parseLong(userIdString);
            } catch (NumberFormatException e) {
                return null; // userId가 숫자가 아닌 경우
            }
        }

        return null;
    }
}