package com.mackessels.fittrackbackend.controller;

import com.mackessels.fittrackbackend.dto.DietPlanRequestDTO;
import com.mackessels.fittrackbackend.dto.DietPlanResponseDTO;
import com.mackessels.fittrackbackend.service.DietService;
import com.mackessels.fittrackbackend.service.AuthenticatedUserService;
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
    private final AuthenticatedUserService authenticatedUserService;

    public DietController(DietService dietService, AuthenticatedUserService authenticatedUserService){
        this.dietService = dietService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @PostMapping
    public ResponseEntity<DietPlanResponseDTO> saveDietPlan(@RequestBody DietPlanRequestDTO dietPlanRequestDTO, @AuthenticationPrincipal Jwt jwt){
        Long userId = authenticatedUserService.getUserId(jwt);
        DietPlanResponseDTO savedDietPlan = dietService.saveDietPlan(dietPlanRequestDTO, userId);
        return new ResponseEntity<>(savedDietPlan, HttpStatus.CREATED);
    }

    @GetMapping
    public List<DietPlanResponseDTO> getAllDietPlans(@AuthenticationPrincipal Jwt jwt){
        return dietService.getAllDietPlans(authenticatedUserService.getUserId(jwt));
    }

    @GetMapping("/{id}")
    public DietPlanResponseDTO getDietPlanById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt){
        return dietService.getDietPlanById(id, authenticatedUserService.getUserId(jwt));
    }

    @DeleteMapping("/{id}")
    public void deleteDietPlan(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt){
        dietService.deleteDietPlan(id, authenticatedUserService.getUserId(jwt));
    }

    @GetMapping("/user/{userId}")
    public DietPlanResponseDTO getDietPlanByUserId(@PathVariable Long userId, @AuthenticationPrincipal Jwt jwt){
        Long authenticatedId = authenticatedUserService.requireOwnUser(userId, jwt);
        return dietService.getDietPlanByUserId(authenticatedId);
    }

}

