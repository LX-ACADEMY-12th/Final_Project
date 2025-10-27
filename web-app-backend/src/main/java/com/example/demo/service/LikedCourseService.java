package com.example.demo.service;

import com.example.demo.dto.LikedCourseListDTO;
import com.example.demo.dto.WishlistItemDTO;
import com.example.demo.mapper.ExhibitionMapper;
import com.example.demo.mapper.PlaceMapper;
import com.example.demo.mapper.WishlistMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LikedCourseService {

    private final WishlistMapper wishlistMapper;
    private final ExhibitionMapper exhibitionMapper;
    private final PlaceMapper placeMapper;

    public LikedCourseService(WishlistMapper wishlistMapper, ExhibitionMapper exhibitionMapper, PlaceMapper placeMapper) {
        this.wishlistMapper = wishlistMapper;
        this.exhibitionMapper = exhibitionMapper;
        this.placeMapper = placeMapper;
    }

    @Transactional(readOnly = true)
    public List<LikedCourseListDTO> getLikedCoursesForUser(Long userId) {
        List<WishlistItemDTO> likedItems = wishlistMapper.findLikedItemsByUserId(userId);
        if (likedItems.isEmpty()) return List.of();

        Map<String, List<Long>> idsByType = likedItems.stream()
                .collect(Collectors.groupingBy(
                        WishlistItemDTO::getTargetType,
                        Collectors.mapping(WishlistItemDTO::getTargetId, Collectors.toList())
                ));

        List<LikedCourseListDTO> results = new ArrayList<>();

        // [!!] DB wishlist 테이블의 target_type 값과 일치해야 함 ('exhibition'?)
        List<Long> exhibitionIds = idsByType.get("exhibition");
        if (exhibitionIds != null && !exhibitionIds.isEmpty()) {
            results.addAll(exhibitionMapper.findExhibitionsForLikedList(exhibitionIds));
        }

        // [!!] DB wishlist 테이블의 target_type 값과 일치해야 함 ('place'?)
        List<Long> placeIds = idsByType.get("place");
        if (placeIds != null && !placeIds.isEmpty()) {
            results.addAll(placeMapper.findPlacesForLikedList(placeIds));
        }

        // 결과 정렬 (선택 사항)
        results.sort((a, b) -> Long.compare(b.getId(), a.getId()));

        return results;
    }
}