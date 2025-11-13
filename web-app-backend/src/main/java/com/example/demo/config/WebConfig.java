package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value; // [추가]
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // application.properties에서 경로를 읽어오도록 변경
    @Value("${file.resource-url-path}")
    private String resourceUrlPath; // /static/images/

    @Value("${file.upload-dir}")
    private String resourceLocation; // C:/uploads/images/

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // [수정] 변수 사용
        // /static/images/** 요청이 오면
        registry.addResourceHandler(resourceUrlPath + "**")
                // file:C:/uploads/images/ 경로에서 파일을 찾음
                .addResourceLocations("file:" + resourceLocation);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}