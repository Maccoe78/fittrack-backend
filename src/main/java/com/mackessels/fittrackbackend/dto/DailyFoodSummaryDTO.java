package com.mackessels.fittrackbackend.dto;

import java.time.LocalDate;
import java.util.List;

public record DailyFoodSummaryDTO(
        LocalDate date,
        List<FoodEntryResponseDTO> entries,
        long consumedCalories,
        Double calorieTarget,
        Double remainingCalories
) {}