package com.example.demo.service;

import java.io.IOException;
import java.util.UUID;
import java.net.URL; // 🟢 Signed URL 생성을 위해
import java.util.concurrent.TimeUnit; // 🟢 Signed URL 시간 설정을 위해

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

// [추가] JWT 및 Security 의존성 Import
import com.example.demo.config.JwtTokenProvider; // 🟢 [수정] config -> jwt 패키지 경로
import com.example.demo.dto.LoginRequestDTO;
import com.example.demo.dto.LoginResponseDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.mapper.UserMapper;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;

//이 클래스가 Spring의 서비스 계층 빈(Bean)임을 나타냅니다.
//비즈니스 로직(데이터 처리)을 담당합니다.
@Service
public class UserService {
	
	// [GCS 추가] Google Storage 객체 주입
    @Autowired
    private Storage storage;

    // [GCS 추가] application.properties 등에서 설정된 버킷 이름을 주입받습니다.
    @Value("${gcs.bucket-name}")
    private String bucketName;

    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider; //
    private final PasswordEncoder passwordEncoder; //

    // 의존성 주입(DI): UserMapper를 자동으로 연결합니다.
    @Autowired
    public UserService(UserMapper userMapper,
                       JwtTokenProvider jwtTokenProvider,
                       PasswordEncoder passwordEncoder,
                       Storage storage) { // 🟢 [추가] Storage 객체를 주입받습니다.
        this.userMapper = userMapper;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.storage = storage; // 🟢 [추가] GCS Storage 객체를 초기화합니다.
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

    // 4. 로그인
    public LoginResponseDTO login(LoginRequestDTO loginRequest) {

        // 1. ID로만 사용자를 조회합니다.
        UserDTO user = userMapper.selectUserByLoginId(loginRequest.getLoginId());

        // 2. 사용자가 존재하고, 암호화된 비밀번호가 일치하는지 확인
        if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            // 인증 실패
            return null;
        }

        // 3. 🟢 DTO를 반환하기 전, Signed URL로 변환합니다.
        UserDTO userWithSignedUrl = convertToSignedUrl(user);

        // 4. 실제 JWT 토큰 생성 (userId 사용)
        String accessToken = jwtTokenProvider.createAccessToken(userWithSignedUrl.getUserId());
        String refreshToken = jwtTokenProvider.createRefreshToken(userWithSignedUrl.getUserId());

        // 5. 수정된 LoginResponseDTO로 반환
        return new LoginResponseDTO(
                userWithSignedUrl.getUserId(),
                userWithSignedUrl.getLoginId(),
                userWithSignedUrl.getName(),
                userWithSignedUrl.getEmail(),
                userWithSignedUrl.getPhoneNumber(),
                userWithSignedUrl.getGender(),
                userWithSignedUrl.getRegion(),
                userWithSignedUrl.getChildGrade(),
                userWithSignedUrl.getProfileImageUrl(), // 🟢 15분짜리 Signed URL
                accessToken,
                refreshToken,
                userWithSignedUrl.getRole()
        );
    }

    /**
     * 5. 🟢 [수정] 'Long' 타입의 userId로 사용자 정보를 조회합니다.
     */
    public UserDTO getUserInfoByUserId(Long userId) {

        UserDTO user = userMapper.selectUserById(userId);
        return convertToSignedUrl(user);
    }

    /**
     * 6. 🟢 [수정] 'Long' 타입의 userId로 회원을 탈퇴시킵니다.
     * (UserController의 withdrawUserById 호출에 맞춤)
     */
    public void withdrawUserById(Long userId) {
        // 🟢 [수정] Mapper에 deleteUserById(Long userId)가 정의되어 있어야 합니다.
        userMapper.deleteUserById(userId);
    }

    // 🟢 [수정] 반환 타입을 int -> UserDTO로 변경, MultipartFile 파라미터 추가
    @Transactional
    public UserDTO updateUserInfo(UserDTO userDTO, MultipartFile profileImage) throws IOException {

        // 🟢 (선택 사항) GCS에서 기존 프로필 이미지 삭제 로직
        // ... (UserMapper에서 기존 profileImageUrl(blobName)을 조회한 후 storage.delete() 호출) ...

        // 🟢 새 프로필 이미지 업로드 (파일이 있는 경우)
        if (profileImage != null && !profileImage.isEmpty()) {
            String blobName = "profiles/" + UUID.randomUUID().toString() + "-" + profileImage.getOriginalFilename();

            BlobId blobId = BlobId.of(bucketName, blobName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(profileImage.getContentType())
                    .build();

            storage.create(blobInfo, profileImage.getBytes());

            // 🟢 DTO에 GCS 객체 이름(blobName)을 저장합니다.
            // (DB에는 전체 URL이 아닌 blobName만 저장해야 ReviewService처럼 Signed URL을 쓸 수 있습니다)
            userDTO.setProfileImageUrl(blobName);
        }

        // 3. 🟢 DB에 사용자 정보 업데이트
        int updatedRows = userMapper.updateUserInfo(userDTO);

        if (updatedRows > 0) {
            // 4. 🟢 DB에서 '최신' 정보를 다시 조회하여 반환
            // (Signed URL 변환 로직이 필요하다면 여기서 추가)
            UserDTO updatedUser = userMapper.selectUserById((long) userDTO.getUserId());
            // (주의: DTO의 profileImageUrl을 Signed URL로 변환해주는 로직이 필요할 수 있습니다)

            return convertToSignedUrl(updatedUser);
        } else {
            return null; // 업데이트 실패
        }
    }

    // 🟢 GCS 객체 이름을 Signed URL로 변환
    /**
     * GCS 객체 이름(blobName)을 15분간 유효한 Signed URL로 변환합니다.
     */
    private String generateSignedUrl(String objectName) {
        if (objectName == null || objectName.isEmpty()) {
            return null;
        }

        try {
            BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, objectName)).build();
            // 15분 제한 시간 설정 (V4 서명 방식)
            URL signedUrl = storage.signUrl(blobInfo, 15, TimeUnit.MINUTES, Storage.SignUrlOption.withV4Signature());
            return signedUrl.toString();
        } catch (Exception e) {
            System.err.println("Signed URL 생성 실패 (Object: " + objectName + "): " + e.getMessage());
            return null;
        }
    }

    /**
     * 🟢 만료된 Signed URL을 갱신하는 전용 로직
     */
    public String getRefreshedProfileUrl(Long userId) {
        // 1. DB에서 사용자 정보 조회
        //    (UserMapper.xml의 'selectUserById' 쿼리가 photo_url AS profileImageUrl을 반환함)
        //    (이때 'user' DTO의 profileImageUrl 필드에는 'blobName'이 담겨있음)
        UserDTO user = userMapper.selectUserById(userId);

        if (user == null || user.getProfileImageUrl() == null || user.getProfileImageUrl().isEmpty()) {
            throw new RuntimeException("프로필 이미지를 찾을 수 없습니다.");
        }

        // 2. user.getProfileImageUrl() (이것은 blobName)을 이용해 새 Signed URL 생성
        String blobName = user.getProfileImageUrl();
        String newSignedUrl = generateSignedUrl(blobName);

        if (newSignedUrl == null) {
            throw new RuntimeException("Signed URL 생성에 실패했습니다.");
        }

        return newSignedUrl;
    }


    // 🟢 [추가] UserDTO의 profileImageUrl을 Signed URL로 변환하는 헬퍼
    /**
     * UserDTO를 받아 profileImageUrl 필드를 Signed URL로 변환합니다.
     */
    private UserDTO convertToSignedUrl(UserDTO user) {
        if (user != null && user.getProfileImageUrl() != null) {
            // DB에 저장된 blobName을 Signed URL로 변환
            String signedUrl = generateSignedUrl(user.getProfileImageUrl());
            // DTO의 필드를 Signed URL로 덮어쓰기
            user.setProfileImageUrl(signedUrl);
        }
        return user;
    }
}

