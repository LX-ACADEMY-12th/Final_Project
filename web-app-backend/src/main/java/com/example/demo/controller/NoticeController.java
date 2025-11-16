package com.example.demo.controller;

import com.example.demo.dto.NoticeDTO;
import com.example.demo.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/notices")
@RequiredArgsConstructor
public class NoticeController {
    
    private final NoticeService noticeService;
    
    // 전체 공지사항 조회
    @GetMapping
    public ResponseEntity<List<NoticeDTO>> getAllNotices() {
        List<NoticeDTO> notices = noticeService.getAllNotices();
        return ResponseEntity.ok(notices);
    }
    
    // ID로 공지사항 조회
    @GetMapping("/{id}")
    public ResponseEntity<NoticeDTO> getNoticeById(@PathVariable Long id) {
        NoticeDTO notice = noticeService.getNoticeById(id);
        if (notice == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(notice);
    }
    
    // 공지사항 생성
    @PostMapping
    public ResponseEntity<NoticeDTO> createNotice(@RequestBody NoticeDTO noticeDTO) {
        NoticeDTO createdNotice = noticeService.createNotice(noticeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdNotice);
    }
    
    // 공지사항 수정
    @PutMapping("/{id}")
    public ResponseEntity<NoticeDTO> updateNotice(@PathVariable Long id, @RequestBody NoticeDTO noticeDTO) {
        NoticeDTO updatedNotice = noticeService.updateNotice(id, noticeDTO);
        return ResponseEntity.ok(updatedNotice);
    }
    
    // 공지사항 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.noContent().build();
    }
}
