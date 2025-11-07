package com.example.demo.mapper;

import com.example.demo.dto.NoticeDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NoticeMapper {
    
    // 전체 공지사항 조회
    List<NoticeDTO> getAllNotices();
    
    // ID로 공지사항 조회
    NoticeDTO getNoticeById(@Param("id") Long id);
    
    // 공지사항 생성
    int createNotice(NoticeDTO noticeDTO);
    
    // 공지사항 수정
    int updateNotice(NoticeDTO noticeDTO);
    
    // 공지사항 삭제
    int deleteNotice(@Param("id") Long id);
    
    // 조회수 증가
    int incrementViews(@Param("id") Long id);
}
