package com.example.demo.controller;

import com.example.demo.dto.ContentResultDTO;
import com.example.demo.dto.ContentSearchDTO;
import com.example.demo.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
public class ContentController {

    @Autowired
    private ContentService contentService;

    @GetMapping("/search")
    public List<ContentResultDTO> searchPlaces(ContentSearchDTO searchDTO) {
        System.out.println("Search Request Received : " + searchDTO);
        return contentService.findPlaces(searchDTO);
    }

}