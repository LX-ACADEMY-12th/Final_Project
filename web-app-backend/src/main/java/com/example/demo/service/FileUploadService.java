package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileUploadService {

    // (주의) 이 경로는 실제 서버 환경에 맞게 설정해야 합니다.
    // (예: C:/uploads/images/ 또는 /var/www/uploads/images/)
    private final String uploadDir = "C:/uploads/images/";

    // (주의) 이 경로는 WebConfig에서 설정할 URL 경로와 일치해야 합니다.
    private final String resourcePath = "/uploads/images/";

    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            String originalName = file.getOriginalFilename();
            String extension = originalName.substring(originalName.lastIndexOf("."));
            String savedName = UUID.randomUUID().toString() + extension;
            
            Path destinationPath = Paths.get(uploadDir + savedName);
            
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs(); // 폴더가 없으면 생성
            }

            file.transferTo(destinationPath);

            // DB에 저장될 URL 경로 반환
            return resourcePath + savedName;

        } catch (Exception e) {
            // (실제로는 로깅을 해야 합니다)
            e.printStackTrace();
            return null;
        }
    }
}