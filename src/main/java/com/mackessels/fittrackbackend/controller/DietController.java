package com.mackessels.fittrackbackend.controller;

import com.mackessels.fittrackbackend.dto.DietPlanRequestDTO;
import com.mackessels.fittrackbackend.dto.DietPlanResponseDTO;
import com.mackessels.fittrackbackend.service.DietService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;
import com.mackessels.fittrackbackend.model.DietPlan;

import java.util.List;

@RestController
@RequestMapping("/diets")
public class DietController {
    private final DietService dietService;

    public DietController(DietService dietService){this.dietService = dietService;}

    @PostMapping
    public ResponseEntity<DietPlanResponseDTO> saveDietPlan(@RequestBody DietPlanRequestDTO dietPlanRequestDTO, @AuthenticationPrincipal Jwt jwt){
        Long userId = authenticatedUserId(jwt);
        DietPlanResponseDTO savedDietPlan = dietService.saveDietPlan(dietPlanRequestDTO, userId);
        return new ResponseEntity<>(savedDietPlan, HttpStatus.CREATED);
    }

    @GetMapping
    public List<DietPlanResponseDTO> getAllDietPlans(@AuthenticationPrincipal Jwt jwt){
        return dietService.getAllDietPlans(authenticatedUserId(jwt));
    }

    @GetMapping("/{id}")
    public DietPlanResponseDTO getDietPlanById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt){
        return dietService.getDietPlanById(id, authenticatedUserId(jwt));
    }

    @DeleteMapping("/{id}")
    public void deleteDietPlan(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt){
        dietService.deleteDietPlan(id, authenticatedUserId(jwt));
    }

    @GetMapping("/user/{userId}")
    public DietPlanResponseDTO getDietPlanByUserId(@PathVariable Long userId, @AuthenticationPrincipal Jwt jwt){
        Long authenticatedId = authenticatedUserId(jwt);

        if (!authenticatedId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Diet not found");
        }

        return dietService.getDietPlanByUserId(authenticatedId);
    }

    private Long authenticatedUserId(Jwt jwt) {
        try {
            long userId = Long.parseLong(jwt.getSubject());
            if (userId <= 0) {
                throw new NumberFormatException();
            }
            return userId;
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid token subject"
            );
        }
    }
}

