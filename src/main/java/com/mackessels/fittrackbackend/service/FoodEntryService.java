package com.mackessels.fittrackbackend.service;

import com.mackessels.fittrackbackend.dto.FoodEntryRequestDTO;
import com.mackessels.fittrackbackend.dto.FoodEntryResponseDTO;
import com.mackessels.fittrackbackend.model.FoodEntry;
import com.mackessels.fittrackbackend.model.User;
import com.mackessels.fittrackbackend.repository.FoodEntryRepository;
import com.mackessels.fittrackbackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.mackessels.fittrackbackend.dto.DailyFoodSummaryDTO;
import com.mackessels.fittrackbackend.repository.DietRepository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class FoodEntryService {

    private static final ZoneId APP_TIME_ZONE = ZoneId.of("Europe/Amsterdam");

    private final FoodEntryRepository foodEntryRepository;
    private final UserRepository userRepository;
    private final DietRepository dietRepository;

    public FoodEntryService(FoodEntryRepository foodEntryRepository, UserRepository userRepository, DietRepository dietRepository) {
        this.foodEntryRepository = foodEntryRepository;
        this.userRepository = userRepository;
        this.dietRepository = dietRepository;
    }

    public FoodEntryResponseDTO addFoodEntry(FoodEntryRequestDTO request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));

        FoodEntry entry = new FoodEntry();
        entry.setUser(user);
        entry.setFoodName(request.foodName().trim());
        entry.setCalories(request.calories());
        entry.setLogDate(LocalDate.now(APP_TIME_ZONE));

        return toResponse(foodEntryRepository.save(entry));
    }

    public List<FoodEntryResponseDTO> getFoodEntries(Long userId, LocalDate date) {
        return foodEntryRepository.findAllByUser_IdAndLogDate(userId, date)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private FoodEntryResponseDTO toResponse(FoodEntry entry) {
        return new FoodEntryResponseDTO(
                entry.getId(),
                entry.getFoodName(),
                entry.getCalories(),
                entry.getLogDate()
        );
    }

    public DailyFoodSummaryDTO getTodaySummary(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        LocalDate today = LocalDate.now(APP_TIME_ZONE);
        List<FoodEntryResponseDTO> entries = getFoodEntries(userId, today);

        long consumedCalories = entries.stream()
                .mapToLong(FoodEntryResponseDTO::calories)
                .sum();

        Double calorieTarget = dietRepository.findByUser_Id(userId)
                .map(dietPlan -> dietPlan.getCalorieTarget())
                .orElse(null);

        Double remainingCalories = calorieTarget == null
                ? null
                : calorieTarget - consumedCalories;

        return new DailyFoodSummaryDTO(
                today, entries, consumedCalories, calorieTarget, remainingCalories
        );
    }
}