package com.mackessels.fittrackbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record FoodEntryRequestDTO(
        @NotBlank
        @Size(max = 255)
        String foodName,

        @NotNull
        @PositiveOrZero
        Integer calories
) {}