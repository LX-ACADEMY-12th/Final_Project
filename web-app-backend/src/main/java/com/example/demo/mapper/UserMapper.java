package com.example.demo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.example.demo.dto.UserDTO;

//@Mapper 어노테이션은 이 인터페이스를 MyBatis 매퍼로 인식하게 하여
//Spring이 이 인터페이스의 메서드에 대응하는 SQL을 UserMapper.xml에서 찾아 실행하도록 합니다.
@Mapper
public interface UserMapper {

    // 1. 아이디 중복 확인: 해당 loginId를 가진 사용자 수를 세어 반환합니다. (변경 없음)
    int countByLoginId(@Param("loginId") String loginId);

    // 2. 이메일 중복 확인: 해당 email을 가진 사용자 수를 세어 반환합니다. (변경 없음)
    int countByEmail(@Param("email") String email);

    // 3. 회원가입: UserDTO 객체를 받아 사용자 정보를 DB에 삽입(INSERT)합니다. (변경 없음)
    void insertUser(UserDTO userDTO);

    // 4. 🔴 [삭제] (UserService에서 암호화 비교 로직으로 대체됨)
    // UserDTO selectUserByLoginIdAndPassword(
    //     @Param("loginId") String loginId,
    //     @Param("password") String password
    // );

    // 5. 🟢 [수정] 'userId' (Long)로 사용자 조회 (PK로 조회)
    // (UserService의 getUserInfoByUserId에서 사용)
    UserDTO selectUserById(@Param("userId") Long userId);

    // 6. 🟢 [추가] 'loginId' (String)로 사용자 조회 (로그인 시 사용)
    // (UserService의 login 메서드에서 사용)
    UserDTO selectUserByLoginId(@Param("loginId") String loginId);

    // 7. 🟢 [수정] 'userId' (Long)로 사용자 삭제
    // (UserService의 withdrawUserById에서 사용)
    void deleteUserById(@Param("userId") Long userId);

    // 8. 🟢 [수정] DTO의 'userId'를 Pk로 사용하여 정보 수정
    // (UserService의 updateUserInfo에서 사용)
    int updateUser(UserDTO userDTO);

    // (참고: 리프레시 토큰 저장을 위한 메서드도 필요할 수 있습니다)
    // void updateRefreshToken(@Param("userId") Long userId, @Param("refreshToken") String refreshToken);

}
