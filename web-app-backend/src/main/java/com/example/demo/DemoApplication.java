package com.example.demo;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@MapperScan("com.example.demo.mapper")
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

    @Bean
    public CommandLineRunner demoData(ExhibitionMapper exhibitionMapper) {
        return args -> {
            // 1. Create tables
            exhibitionMapper.createSciencePlaceTable();
            exhibitionMapper.createExhibitionTable();
            exhibitionMapper.createExhibitionCourseTable();

            // 2. Create SciencePlace data
            SciencePlace gwacheon = new SciencePlace("국립과천과학관", "국내 최대 과학관", 37.4769, 126.9810);
            SciencePlace seoulGrandPark = new SciencePlace("서울대공원", "동물원과 식물원", 37.4801, 126.9522);
            SciencePlace centralMuseum = new SciencePlace("국립중앙박물관", "한국의 역사와 문화", 37.5582, 126.9734);

            exhibitionMapper.saveSciencePlace(gwacheon);
            exhibitionMapper.saveSciencePlace(seoulGrandPark);
            exhibitionMapper.saveSciencePlace(centralMuseum);

            // 3. Create Exhibition data for 4th grade
            Exhibition exhibition4th = new Exhibition("4학년: 우리 몸의 비밀 탐험", "생물", "국립과천과학관", "상시", 4, null);
            exhibitionMapper.saveExhibition(exhibition4th);
            exhibitionMapper.saveExhibitionCourseLink(exhibition4th.getId(), gwacheon.getId(), 0);
            exhibitionMapper.saveExhibitionCourseLink(exhibition4th.getId(), centralMuseum.getId(), 1);
            exhibitionMapper.saveExhibitionCourseLink(exhibition4th.getId(), seoulGrandPark.getId(), 2);

            // 4. Create Exhibition data for 5th grade
            Exhibition exhibition5th = new Exhibition("5학년: 태양계와 별자리 여행", "지구과학", "국립과천과학관", "2025.09.01 ~ 2025.11.30", 5, null);
            exhibitionMapper.saveExhibition(exhibition5th);
            exhibitionMapper.saveExhibitionCourseLink(exhibition5th.getId(), gwacheon.getId(), 0);
            exhibitionMapper.saveExhibitionCourseLink(exhibition5th.getId(), seoulGrandPark.getId(), 1);
        };
    }
}
