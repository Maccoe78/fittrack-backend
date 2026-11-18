package com.mackessels.fittrackbackend.dto;

import java.time.LocalDate;

public record FoodEntryResponseDTO(
        Long id,
        String foodName,
        int calories,
        LocalDate logDate
) {}