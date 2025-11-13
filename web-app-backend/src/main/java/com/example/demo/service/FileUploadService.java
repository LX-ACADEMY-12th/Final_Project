package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value; // [추가]
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileUploadService {

    private final String uploadDir;
    private final String resourcePath;

    // [수정] 생성자를 통해 properties 값 주입
    public FileUploadService(@Value("${file.upload-dir}") String uploadDir,
                             @Value("${file.resource-url-path}") String resourcePath) {
        this.uploadDir = uploadDir; // C:/uploads/images/
        this.resourcePath = resourcePath; // /static/images/
    }

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

            // [수정] DB에 저장될 URL 경로 반환 (e.g., /static/images/uuid.jpg)
            return resourcePath + savedName;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void deleteImage(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith(resourcePath)) {
            return;
        }
        try {
            String fileName = imageUrl.substring(resourcePath.length());
            Path filePath = Paths.get(uploadDir + fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}