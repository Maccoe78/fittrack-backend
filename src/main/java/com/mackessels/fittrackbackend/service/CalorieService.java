package com.mackessels.fittrackbackend.service;

import com.mackessels.fittrackbackend.model.DietPlan;
import com.mackessels.fittrackbackend.model.Gender;
import com.mackessels.fittrackbackend.repository.DietRepository;
import org.springframework.stereotype.Service;

@Service
public class CalorieService {

    private final DietRepository dietRepository;

    public CalorieService(DietRepository dietRepository) {
        this.dietRepository = dietRepository;
    }

    public double calculateCalories(DietPlan dietPlan){
        double bmr = 0;
        int age = dietPlan.getAge();
        double weight = dietPlan.getWeight();
        double height = dietPlan.getHeight();

        if (dietPlan.getGender() == Gender.MALE){
        bmr = 10 * weight + 6.25 * height - 5 * age + 5;
        } else if (dietPlan.getGender() == Gender.FEMALE){
        bmr = 10 * weight + 6.25 * height - 5 * age -161;
        } else if (dietPlan.getGender() == Gender.OTHER) {
            bmr = 10 * weight + 6.25 * height - 5 * age + 5;
        }

        double activityMultiplier;

        switch (dietPlan.getActivityLevel()) {
            case SEDENTARY :
                activityMultiplier = 1.2;
                break;
            case LIGHT :
                activityMultiplier = 1.357;
                break;
            case MODERATE :
                activityMultiplier = 1.55;
                break;
            case VERY_ACTIVE :
                activityMultiplier = 1.725;
                break;
            case EXTRA_ACTIVE :
                activityMultiplier = 1.9;
                break;
            default:
                throw new IllegalArgumentException("Unknown activity level");
        }

        double tdee = bmr * activityMultiplier;
        double calories = tdee;

        double paceAdjustment;
        String goalType;

        if (dietPlan.getGoalWeight() < weight) {
            goalType = "cut";
        } else if (dietPlan.getGoalWeight() > weight) {
            goalType = "bulk";
        } else if (dietPlan.getGoalWeight() == weight) {
            goalType = "maintain";
        } else {
            goalType = "maintain";
        }

        switch (dietPlan.getPace()) {
            case SLOW:
                paceAdjustment = 250;
                break;
            case NORMAL:
                paceAdjustment = 500;
                break;
            case FAST:
                paceAdjustment = 750;
                break;
            default:
                throw new IllegalArgumentException("Unknown pace level");
        }

        if ("cut".equals(goalType)){
            calories -= paceAdjustment;
        } else if ("bulk".equals(goalType)) {
            calories += paceAdjustment;
        } else if ("maintain".equals(goalType)){

        }

        dietPlan.setCalorieTarget(calories);
        return calories;
    }
}
