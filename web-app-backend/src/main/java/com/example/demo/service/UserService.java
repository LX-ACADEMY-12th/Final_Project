package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.LoginRequestDTO;
import com.example.demo.dto.LoginResponseDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.mapper.UserMapper;

//이 클래스가 Spring의 서비스 계층 빈(Bean)임을 나타냅니다.
//비즈니스 로직(데이터 처리)을 담당합니다.
@Service
public class UserService {

	 private final UserMapper userMapper;
	
	 // 의존성 주입(DI): UserMapper를 자동으로 연결합니다.
	 @Autowired
	 public UserService(UserMapper userMapper) {
	     this.userMapper = userMapper;
	 }
	
	 // 1. 아이디 중복 확인
	 public boolean checkIdDuplicate(String loginId) {
	     // 중복되면 1 이상, 아니면 0을 반환합니다.
	     return userMapper.countByLoginId(loginId) > 0;
	 }
	
	 // 2. 이메일 중복 확인
	 public boolean checkEmailDuplicate(String email) {
	     return userMapper.countByEmail(email) > 0;
	 }
	
	 // 3. 회원가입
	 public void registerUser(UserDTO userDTO) {
	     // 실제 프로젝트에서는 비밀번호를 반드시 BCrypt 등으로 암호화해야 합니다.
	     // 여기서는 암호화 과정 없이 DTO를 그대로 Mapper에 전달합니다.
	     userMapper.insertUser(userDTO);
	 }
	
	 // 4. 로그인
	 public LoginResponseDTO login(LoginRequestDTO loginRequest) {
	     // 실제 프로젝트에서는 입력된 비밀번호를 암호화하여 DB에 저장된 암호화된 비밀번호와 비교해야 합니다.
	     // 여기서는 간단히 아이디와 평문 비밀번호를 이용해 사용자 정보를 조회합니다.
	     UserDTO user = userMapper.selectUserByLoginIdAndPassword(
	         loginRequest.getLoginId(), 
	         loginRequest.getPassword()
	     );
	
	     if (user == null) {
	         // 인증 실패
	         return null;
	     }
	
	     // 인증 성공 시, 사용자 정보와 토큰을 담아 반환
	     LoginResponseDTO responseDTO = new LoginResponseDTO();
	     responseDTO.setUserId(user.getUserId());
	     responseDTO.setLoginId(user.getLoginId());
	     responseDTO.setName(user.getName());
	     
	     // (모의) JWT 토큰 생성: 실제로는 라이브러리를 사용해야 합니다.
	     // 여기서는 간단히 "loginId_timestamp" 형태의 문자열을 토큰으로 가정합니다.
	     String token = user.getLoginId() + "_" + System.currentTimeMillis();
	     responseDTO.setToken(token);
	
	     return responseDTO;
	 }
	 
	 // 5. 로그인 아이디로 사용자 정보를 조회합니다.
	 public UserDTO getUserInfoByLoginId(String loginId) {
	     return userMapper.selectUserByLoginId(loginId);
	 }
	
	 // 6. 회원탈퇴 로직 
	 public void withdrawUser(String loginId) {
	     // UserMapper의 메서드를 호출하여 해당 loginId를 가진 사용자를 DB에서 삭제합니다.
	     userMapper.deleteUserByLoginId(loginId);
	 }
	 
	 // 7. 사용자 정보 업데이트
	 public int updateUserInfo(UserDTO userDTO) {
	     // Mapper를 호출하여 DTO에 담긴 정보로 사용자를 업데이트합니다.
	     // loginId를 WHERE 조건으로 사용합니다.
	     return userMapper.updateUser(userDTO);
	 }
 
}