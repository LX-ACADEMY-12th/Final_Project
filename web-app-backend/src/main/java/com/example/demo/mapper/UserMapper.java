package com.example.demo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.example.demo.dto.UserDTO;

//@Mapper 어노테이션은 이 인터페이스를 MyBatis 매퍼로 인식하게 하여
//Spring이 이 인터페이스의 메서드에 대응하는 SQL을 UserMapper.xml에서 찾아 실행하도록 합니다.
@Mapper
public interface UserMapper {
 
 // 1. 아이디 중복 확인: 해당 loginId를 가진 사용자 수를 세어 반환합니다.
 int countByLoginId(@Param("loginId") String loginId);

 // 2. 이메일 중복 확인: 해당 email을 가진 사용자 수를 세어 반환합니다.
 int countByEmail(@Param("email") String email);

 // 3. 회원가입: UserDTO 객체를 받아 사용자 정보를 DB에 삽입(INSERT)합니다.
 void insertUser(UserDTO userDTO);

 // 4. 로그인: loginId와 password를 모두 사용하여 일치하는 사용자 정보를 조회합니다.
 UserDTO selectUserByLoginIdAndPassword(
     @Param("loginId") String loginId,
     @Param("password") String password
 );

 // 5. 사용자 정보 조회: loginId를 기준으로 사용자 상세 정보를 조회합니다.
 UserDTO selectUserByLoginId(@Param("loginId") String loginId);

 // 6. 회원탈퇴: loginId를 기준으로 사용자를 DB에서 삭제(DELETE)합니다.
 void deleteUserByLoginId(@Param("loginId") String loginId);
 
 //7. 사용자 정보 업데이트: DTO를 받아 loginId를 기준으로 정보를 수정합니다.
 int updateUser(UserDTO userDTO);
 
}