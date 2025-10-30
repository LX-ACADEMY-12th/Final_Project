package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping; // ⬅️ 경로 수정
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.UserWishListDTO;
import com.example.demo.dto.WishListDTO;
import com.example.demo.service.WishListService;

@RestController
@RequestMapping("/api/wishlist")
public class WishListController {

	@Autowired
	private WishListService wishlistService;
	
	private final Long TEMP_USER_ID = 1L;
	
	/**
	 * 상세 페이지에서 찜하는 api (중복 검사 포함)
	 */
	@PostMapping // ⬅️ 1. 경로 수정: @PostMapping("{}") -> @PostMapping
	public ResponseEntity<String> addWishlist(@RequestBody WishListDTO wishlistDto) {
        
        // ⬅️ 2. 서비스의 반환값(boolean)을 받도록 수정
        boolean addedSuccessfully = wishlistService.addWishlist(
            TEMP_USER_ID, 
            wishlistDto.getTargetType(), 
            wishlistDto.getTargetId()
        );
        
        // ⬅️ 3. 반환값에 따라 다른 응답을 주도록 수정
        if (addedSuccessfully) {
            // 200 OK
            return ResponseEntity.ok("찜 목록에 추가되었습니다.");
        } else {
            // 409 Conflict (충돌)
            return ResponseEntity.status(409).body("이미 찜한 항목입니다.");
        }
	}
	
	/**
     * 2. 찜 취소 (삭제) - (이 부분은 완벽합니다)
     */
    @DeleteMapping
    public ResponseEntity<String> removeWishlist(@RequestBody WishListDTO wishlistDto) {
        
        int affectedRows = wishlistService.removeWishlist(
            TEMP_USER_ID, 
            wishlistDto.getTargetType(), 
            wishlistDto.getTargetId()
        );

        if (affectedRows > 0) {
            return ResponseEntity.ok("찜 목록에서 삭제되었습니다.");
        } else {
            return ResponseEntity.status(404).body("삭제할 찜 항목을 찾을 수 없습니다.");
        }
    }
    
    /**
     * 3. 내 찜 목록 조회 (마이페이지 /likePlace 용) - (이 부분도 완벽합니다)
     */
    @GetMapping("/my-list")
    public ResponseEntity<List<UserWishListDTO>> getMyWishlist() {
        
        List<UserWishListDTO> myList = wishlistService.findMyWishlistByUserId(TEMP_USER_ID);
        
        return ResponseEntity.ok(myList);
    }
}