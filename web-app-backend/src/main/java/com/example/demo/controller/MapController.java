package com.example.demo.controller;

import com.example.demo.dto.PlaceResultDTO;
import com.example.demo.dto.PlaceSearchDTO;
import com.example.demo.service.MapService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class MapController {

    @Autowired
    private MapService mapService;

    @GetMapping("/search")
    public List<PlaceResultDTO> searchPlaces(PlaceSearchDTO searchDTO) {
        System.out.println("Search Request Received : " + searchDTO);
        return mapService.findPlaces(searchDTO);
    }

}