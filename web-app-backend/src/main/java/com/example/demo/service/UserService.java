package com.example.demo.service;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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
    private final Storage storage;

    // [GCS 추가] application.properties 등에서 설정된 버킷 이름을 주입받습니다.
    @Value("${gcp.storage.bucket-name}")
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
                user.getPhoneNumber(),
                user.getGender(),
                user.getRegion(),
                user.getChildGrade(),
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
    
 // ----------------------------------------------------------------------
    // 8. 🟢 [추가] 사용자의 프로필 사진을 GCS에 저장하고, 경로를 DB에 업데이트하는 로직
    // ----------------------------------------------------------------------

    /**
     * 사용자의 프로필 사진을 GCS에 저장하고, 저장된 파일 경로(blobName)를 DB에 업데이트하는 메서드
     * @param userId 사진을 업로드할 사용자 ID
     * @param file 사용자가 업로드한 프로필 사진 파일 (MultipartFile)
     * @return GCS에 저장된 객체 이름 (blobName)
     */
    public String uploadUserProfilePhoto(Long userId, MultipartFile file) {

        // 1. 파일이 유효한지 확인합니다.
        if (file == null || file.isEmpty()) {
            // 파일이 없으면 null을 반환하거나 예외를 발생시킵니다.
            return null;
        }

        String originalName = file.getOriginalFilename();
        String extension = ""; // 파일 확장자를 저장할 변수

        // 2. 파일 이름에서 확장자를 추출합니다.
        if (originalName != null && originalName.contains(".")) {
            // 마지막 점(.) 이후의 문자열을 확장자로 가져옵니다.
            extension = originalName.substring(originalName.lastIndexOf("."));
        }

        // 3. GCS에 저장할 고유한 객체 이름(Blob Name)을 생성합니다.
        // "user/" 경로로 시작하고, UUID를 사용하여 파일 이름의 중복을 방지합니다.
        String blobName = "user/" + UUID.randomUUID().toString() + extension;

        try {
            // 4. GCS 업로드를 위한 BlobId와 BlobInfo 객체를 생성합니다.
            // 어느 버킷(bucketName)에 어떤 이름(blobName)으로 저장할지 지정합니다.
            BlobId blobId = BlobId.of(bucketName, blobName);
            // Blob의 메타 정보(Content-Type)를 설정합니다.
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();

            // 5. GCS에 실제 파일을 업로드(생성)합니다.
            // blobInfo에 정의된 정보와 파일의 바이트 배열을 사용하여 저장합니다.
            storage.create(blobInfo, file.getBytes());

            // 6. 🔴 DB에 GCS 객체 이름을 저장합니다.
            // 생성된 GCS 객체 이름(blobName)을 해당 사용자 ID의 프로필 사진 경로로 DB에 업데이트합니다.
            // userMapper에 updateUserProfilePhoto(Long userId, String blobName) 메서드가 필요합니다.
            userMapper.updateUserProfilePhoto(userId, blobName);

            // 7. 성공적으로 저장된 GCS 객체 이름(경로)을 반환합니다.
            return blobName;

        } catch (IOException e) {
            // 파일 처리 또는 GCS 업로드 중 IO 오류 발생 시 예외를 던집니다.
            throw new RuntimeException("GCP Storage 프로필 파일 업로드 실패", e);
        }
    }

}

