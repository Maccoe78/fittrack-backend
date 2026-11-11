package com.mackessels.fittrackbackend.dto;

public record UpdateUserRequestDTO(
        String currentPassword,
        String name,
        String newPassword
) {}