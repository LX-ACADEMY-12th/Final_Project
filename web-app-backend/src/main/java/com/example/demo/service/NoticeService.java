package com.example.demo.service;

import com.example.demo.dto.NoticeDTO;
import com.example.demo.mapper.NoticeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {
    
    private final NoticeMapper noticeMapper;
    
    // 전체 공지사항 조회
    public List<NoticeDTO> getAllNotices() {
        return noticeMapper.getAllNotices();
    }
    
    // ID로 공지사항 조회
    public NoticeDTO getNoticeById(Long id) {
        noticeMapper.incrementViews(id);  // 조회수 증가
        return noticeMapper.getNoticeById(id);
    }
    
    // 공지사항 생성
    public NoticeDTO createNotice(NoticeDTO noticeDTO) {
        noticeMapper.createNotice(noticeDTO);
        return noticeDTO;
    }
    
    // 공지사항 수정
    public NoticeDTO updateNotice(Long id, NoticeDTO noticeDTO) {
        noticeDTO.setId(id);
        noticeMapper.updateNotice(noticeDTO);
        return noticeDTO;
    }
    
    // 공지사항 삭제
    public void deleteNotice(Long id) {
        noticeMapper.deleteNotice(id);
    }
}
