package com.mackessels.fittrackbackend.controller;

import com.mackessels.fittrackbackend.dto.DietPlanRequestDTO;
import com.mackessels.fittrackbackend.dto.DietPlanResponseDTO;
import com.mackessels.fittrackbackend.service.DietService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.mackessels.fittrackbackend.model.DietPlan;

import java.util.List;

@RestController
@RequestMapping("/diets")
public class DietController {
    private final DietService dietService;

    public DietController(DietService dietService){this.dietService = dietService;}

    @PostMapping
    public ResponseEntity<DietPlanResponseDTO> saveDietPlan(@RequestBody DietPlanRequestDTO dietPlanRequestDTO){
        DietPlanResponseDTO savedDietPlan = dietService.saveDietPlan(dietPlanRequestDTO);
        return new ResponseEntity<>(savedDietPlan, HttpStatus.CREATED);
    }

    @GetMapping
    public List<DietPlanResponseDTO> getAllDietPlans(){
        return dietService.getAllDietPlans();
    }

    @GetMapping("/{id}")
    public DietPlanResponseDTO getDietPlanById(@PathVariable Long id){
        return dietService.getDietPlanById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteDietPlan(@PathVariable Long id){
        dietService.deleteDietPlan(id);
    }

}

