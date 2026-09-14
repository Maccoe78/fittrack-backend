package com.mackessels.fittrackbackend;

import com.mackessels.fittrackbackend.service.CalorieService;
import com.mackessels.fittrackbackend.model.ActivityLevel;
import com.mackessels.fittrackbackend.model.DietPlan;
import com.mackessels.fittrackbackend.model.Gender;
import com.mackessels.fittrackbackend.model.Pace;
import com.mackessels.fittrackbackend.repository.DietRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class CalorieServiceTest {

    @Test
    void shouldCalculateCalories() {
        DietRepository dietRepository = Mockito.mock(DietRepository.class);
        CalorieService calorieService = new CalorieService(dietRepository);

        DietPlan dietPlan = new DietPlan();
        dietPlan.setAge(29);
        dietPlan.setWeight(100);
        dietPlan.setHeight(190);
        dietPlan.setGoalWeight(80);
        dietPlan.setGender(Gender.MALE);
        dietPlan.setActivityLevel(ActivityLevel.MODERATE);
        dietPlan.setPace(Pace.SLOW);

        calorieService.calculateCalories(dietPlan);

        assertNotNull(dietPlan.getCalorieTarget());
    }
}