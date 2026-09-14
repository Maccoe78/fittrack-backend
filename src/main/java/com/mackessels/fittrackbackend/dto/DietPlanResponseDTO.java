package com.mackessels.fittrackbackend.dto;

import com.mackessels.fittrackbackend.model.ActivityLevel;
import com.mackessels.fittrackbackend.model.Gender;
import com.mackessels.fittrackbackend.model.Pace;

public class DietPlanResponseDTO {
    private Long id;
    private int age;
    private double weight;
    private double height;
    private double goalWeight;
    private Gender gender;
    private ActivityLevel activityLevel;
    private Pace pace;
    private double calorieTarget;
    private Long userId;

    public DietPlanResponseDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getGoalWeight() {
        return goalWeight;
    }

    public void setGoalWeight(double goalWeight) {
        this.goalWeight = goalWeight;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public ActivityLevel getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(ActivityLevel activityLevel) {
        this.activityLevel = activityLevel;
    }

    public Pace getPace() {
        return pace;
    }

    public void setPace(Pace pace) {
        this.pace = pace;
    }

    public double getCalorieTarget() {
        return calorieTarget;
    }

    public void setCalorieTarget(double calorieTarget) {
        this.calorieTarget = calorieTarget;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}