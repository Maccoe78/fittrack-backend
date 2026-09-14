package com.mackessels.fittrackbackend.service;

import com.mackessels.fittrackbackend.dto.DietPlanRequestDTO;
import com.mackessels.fittrackbackend.dto.DietPlanResponseDTO;
import com.mackessels.fittrackbackend.model.DietPlan;
import com.mackessels.fittrackbackend.repository.DietRepository;
import com.mackessels.fittrackbackend.repository.UserRepository;
import com.mackessels.fittrackbackend.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class DietService {

    private final DietRepository dietRepository;
    private final CalorieService calorieService;
    private final UserRepository userRepository;

    public DietService(DietRepository dietRepository, CalorieService calorieService, UserRepository userRepository) {
        this.dietRepository = dietRepository;
        this.calorieService = calorieService;
        this.userRepository = userRepository;
    }

    public DietPlanResponseDTO saveDietPlan(DietPlanRequestDTO dietPlanRequestDTO) {
        User user = userRepository.findById(dietPlanRequestDTO.getUserId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with id = " + dietPlanRequestDTO.getUserId()
                ));

        DietPlan dietPlan = new DietPlan();
        dietPlan.setAge(dietPlanRequestDTO.getAge());
        dietPlan.setWeight(dietPlanRequestDTO.getWeight());
        dietPlan.setHeight(dietPlanRequestDTO.getHeight());
        dietPlan.setGoalWeight(dietPlanRequestDTO.getGoalWeight());
        dietPlan.setGender(dietPlanRequestDTO.getGender());
        dietPlan.setActivityLevel(dietPlanRequestDTO.getActivityLevel());
        dietPlan.setPace(dietPlanRequestDTO.getPace());
        dietPlan.setUser(user);

        calorieService.calculateCalories(dietPlan);

        DietPlan savedDietPlan = dietRepository.save(dietPlan);

        return toResponseDTO(savedDietPlan);
    }

    public List<DietPlanResponseDTO> getAllDietPlans() {
        return dietRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public DietPlanResponseDTO getDietPlanById(Long id) {
        DietPlan dietPlan = dietRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Diet not found with id = " + id
                ));

        return toResponseDTO(dietPlan);
    }

    public void deleteDietPlan(Long id) {
        if (!dietRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Diet not found by id: " + id
            );
        }
        dietRepository.deleteById(id);
    }

    private DietPlanResponseDTO toResponseDTO(DietPlan dietPlan) {
        DietPlanResponseDTO responseDTO = new DietPlanResponseDTO();
        responseDTO.setId(dietPlan.getId());
        responseDTO.setAge(dietPlan.getAge());
        responseDTO.setWeight(dietPlan.getWeight());
        responseDTO.setHeight(dietPlan.getHeight());
        responseDTO.setGoalWeight(dietPlan.getGoalWeight());
        responseDTO.setGender(dietPlan.getGender());
        responseDTO.setActivityLevel(dietPlan.getActivityLevel());
        responseDTO.setPace(dietPlan.getPace());
        responseDTO.setCalorieTarget(dietPlan.getCalorieTarget());

        if (dietPlan.getUser() != null) {
            responseDTO.setUserId(dietPlan.getUser().getId());
        }

        return responseDTO;
    }
}