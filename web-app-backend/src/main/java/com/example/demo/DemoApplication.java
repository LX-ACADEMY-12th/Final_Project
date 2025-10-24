package com.example.demo;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

// 1. 테스트에 필요한 DTO와 Service를 임포트합니다.
import com.example.demo.dto.ExhibitionDetailDTO;
import com.example.demo.service.ExhibitionDetailService;

@SpringBootApplication
@MapperScan("com.example.demo.mapper")
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

    /**
     * 애플리케이션 시작 시, ExhibitionDetailService를 테스트하는
     * CommandLineRunner를 Bean으로 등록합니다.
     */
    @Bean
    public CommandLineRunner run(ExhibitionDetailService exhibitionDetailService) {
        // 2. 이 메서드 파라미터로 받고 싶은 Bean(Service 등)을 선언하면
        //    Spring이 자동으로 주입(@Autowired)해 줍니다.
        
        return (args) -> {
            // 3. 애플리케이션 시작 시 실행할 코드를 여기에 작성합니다.
            
            // --- 테스트 시작 ---
            System.out.println("--- [TEST] ExhibitionDetailService를 테스트합니다... ---");
            
            // 4. DB에 실제로 존재하는 전시 ID를 넣으세요 (예: 1L 또는 101L)
            Long testId = 1L; 
            
            try {
                ExhibitionDetailDTO dto = exhibitionDetailService.getfindExhibitionDetails(testId);
                
                if (dto != null) {
                    // 5. 성공 시
                    System.out.println("✅ [SUCCESS] ID " + testId + "번 전시 정보를 찾았습니다.");
                    System.out.println("  -> 전시명: " + dto.getExhibitionName());
                    System.out.println("  -> 타입: " + dto.getType());
                } else {
                    // 6. 실패 시 (ID가 없는 경우)
                    System.out.println("❌ [FAIL] ID " + testId + "번 전시 정보를 찾을 수 없습니다. (결과가 null입니다)");
                }
            } catch (Exception e) {
                // 7. DB 연결 오류 등 예외 발생 시
                System.out.println("🔥 [ERROR] 테스트 중 오류가 발생했습니다: " + e.getMessage());
                e.printStackTrace(); // (자세한 에러 로그)
            }
            
            System.out.println("--- [TEST] 테스트가 종료되었습니다. ---");
            // --- 테스트 끝 ---
        };
    }
}