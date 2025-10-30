package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.LoginRequestDTO;
import com.example.demo.dto.LoginResponseDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.mapper.UserMapper;

// [추가] JWT 및 Security 의존성 Import
import com.example.demo.config.JwtTokenProvider; // 🟢 [수정] config -> jwt 패키지 경로
import org.springframework.security.crypto.password.PasswordEncoder;

//이 클래스가 Spring의 서비스 계층 빈(Bean)임을 나타냅니다.
//비즈니스 로직(데이터 처리)을 담당합니다.
@Service
public class UserService {

    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider; //
    private final PasswordEncoder passwordEncoder; //

    // 의존성 주입(DI): UserMapper를 자동으로 연결합니다.
    @Autowired
    public UserService(UserMapper userMapper,
                       JwtTokenProvider jwtTokenProvider,
                       PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. 아이디 중복 확인 (변경 없음)
    public boolean checkIdDuplicate(String loginId) {
        // 중복되면 1 이상, 아니면 0을 반환합니다.
        return userMapper.countByLoginId(loginId) > 0;
    }

    // 2. 이메일 중복 확인 (변경 없음)
    public boolean checkEmailDuplicate(String email) {
        return userMapper.countByEmail(email) > 0;
    }

    // 3. 회원가입 (변경 없음)
    public void registerUser(UserDTO userDTO) {
        // 🟢 비밀번호를 BCrypt 등으로 암호화합니다.
        String encodedPassword = passwordEncoder.encode(userDTO.getPassword());
        userDTO.setPassword(encodedPassword);

        userMapper.insertUser(userDTO);
    }

    // 4. 로그인 (변경 없음)
    public LoginResponseDTO login(LoginRequestDTO loginRequest) {

        // 1. ID로만 사용자를 조회합니다.
        UserDTO user = userMapper.selectUserByLoginId(loginRequest.getLoginId());

        // 2. 사용자가 존재하고, 암호화된 비밀번호가 일치하는지 확인
        if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            // 인증 실패
            return null;
        }

        // 3. 실제 JWT 토큰 생성 (userId 사용)
        String accessToken = jwtTokenProvider.createAccessToken(user.getUserId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());

        // (참고: Refresh Token은 DB에 저장(업데이트)하는 로직이 권장됩니다.)
        // userMapper.updateRefreshToken(user.getUserId(), refreshToken);

        // 4. 수정된 LoginResponseDTO로 반환
        return new LoginResponseDTO(
                user.getUserId(),
                user.getLoginId(),
                user.getName(),
                user.getEmail(),
                accessToken,    // 👈 실제 Access Token
                refreshToken    // 👈 실제 Refresh Token
        );
    }

    /**
     * 5. 🟢 [수정] 'Long' 타입의 userId로 사용자 정보를 조회합니다.
     */
    public UserDTO getUserInfoByUserId(Long userId) {
        // 🟢 [수정] Mapper에 selectUserById(Long userId)가 정의되어 있어야 합니다.
        return userMapper.selectUserById(userId);
    }

    /**
     * 6. 🟢 [수정] 'Long' 타입의 userId로 회원을 탈퇴시킵니다.
     * (UserController의 withdrawUserById 호출에 맞춤)
     */
    public void withdrawUserById(Long userId) {
        // 🟢 [수정] Mapper에 deleteUserById(Long userId)가 정의되어 있어야 합니다.
        userMapper.deleteUserById(userId);
    }

    /**
     * 7. 🟢 [수정] 사용자 정보 업데이트
     * (UserController가 DTO에 userId를 설정해서 넘겨줍니다)
     */
    public int updateUserInfo(UserDTO userDTO) {
        // 🟢 [수정] Mapper의 updateUser가 DTO의 'userId'를 WHERE 조건으로 사용해야 합니다.
        return userMapper.updateUser(userDTO);
    }

}

