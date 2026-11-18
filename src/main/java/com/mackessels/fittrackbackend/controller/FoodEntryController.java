package com.mackessels.fittrackbackend.controller;

import com.mackessels.fittrackbackend.dto.FoodEntryRequestDTO;
import com.mackessels.fittrackbackend.dto.FoodEntryResponseDTO;
import com.mackessels.fittrackbackend.service.AuthenticatedUserService;
import com.mackessels.fittrackbackend.service.FoodEntryService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import com.mackessels.fittrackbackend.dto.DailyFoodSummaryDTO;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/food-entries")
public class FoodEntryController {

    private final FoodEntryService foodEntryService;
    private final AuthenticatedUserService authenticatedUserService;

    public FoodEntryController(FoodEntryService foodEntryService, AuthenticatedUserService authenticatedUserService) {
        this.foodEntryService = foodEntryService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @PostMapping
    public ResponseEntity<FoodEntryResponseDTO> addFoodEntry(@Valid @RequestBody FoodEntryRequestDTO request, @AuthenticationPrincipal Jwt jwt) {
        Long userId = authenticatedUserService.getUserId(jwt);

        FoodEntryResponseDTO savedEntry =
                foodEntryService.addFoodEntry(request, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedEntry);
    }

    @GetMapping
    public List<FoodEntryResponseDTO> getFoodEntries(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, @AuthenticationPrincipal Jwt jwt) {
        Long userId = authenticatedUserService.getUserId(jwt);
        return foodEntryService.getFoodEntries(userId, date);
    }

    @GetMapping("/today/summary")
    public DailyFoodSummaryDTO getTodaySummary(@AuthenticationPrincipal Jwt jwt) {
        Long userId = authenticatedUserService.getUserId(jwt);
        return foodEntryService.getTodaySummary(userId);
    }
}