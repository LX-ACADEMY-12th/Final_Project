package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItemDTO {
    private String targetType; // 'exhibition' or 'place'
    private Long targetId;     // exhibition_id or place_id
}
