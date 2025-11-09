package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/api/**") // '/api/'로 시작하는 모든 경로에 대해
//                .allowedOrigins("http://localhost:5173") // Vue 개발 서버의 주소를 허용
//                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP 메소드
//                .allowedHeaders("*") // 모든 헤더 허용
//                .allowCredentials(true); // 쿠키 등 자격 증명 허용
//    }

    // FileUploadService가 저장한 이미지를 웹에서 접근 가능하도록 설정
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String resourceUrlPath = "/uploads/images/**";
        String resourceLocation = "file:C:/uploads/images/";
        registry.addResourceHandler(resourceUrlPath)
                .addResourceLocations(resourceLocation);
    }

}