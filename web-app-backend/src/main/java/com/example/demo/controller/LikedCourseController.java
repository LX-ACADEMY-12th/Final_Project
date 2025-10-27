package com.example.demo.controller;

import com.example.demo.dto.LikedCourseListDTO;
import com.example.demo.service.LikedCourseService;
// import org.springframework.security.core.annotation.AuthenticationPrincipal;
// import com.example.demo.security.UserDetailsImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/liked-courses")
public class LikedCourseController {

    private final LikedCourseService likedCourseService;

    public LikedCourseController(LikedCourseService likedCourseService) {
        this.likedCourseService = likedCourseService;
    }

    @GetMapping("/me")
    public ResponseEntity<List<LikedCourseListDTO>> getMyLikedCourses(
            // @AuthenticationPrincipal UserDetailsImpl userDetails // 실제 사용자 ID 가져오기
    ) {
        Long userId = 1L; // [!!] 임시 사용자 ID, 실제 인증 정보로 교체 필요
        if (userId == null) return ResponseEntity.status(401).build();

        List<LikedCourseListDTO> likedCourses = likedCourseService.getLikedCoursesForUser(userId);
        return ResponseEntity.ok(likedCourses);
    }
}