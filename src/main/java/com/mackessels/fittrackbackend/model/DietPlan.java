package com.mackessels.fittrackbackend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "diets")
public class DietPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int age;
    private double weight;
    private double height;
    private double goalWeight;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private ActivityLevel activityLevel;

    @Enumerated(EnumType.STRING)
    private Pace pace;

    private double calorieTarget;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public DietPlan(){

    }

    public DietPlan(Long id, int age, double weight, double height, double goalWeight, Gender gender, ActivityLevel activityLevel, Pace pace, double calorieTarget, User user){
        this.id = id;
        this.age = age;
        this.weight = weight;
        this.height = height;
        this.gender = gender;
        this.activityLevel = activityLevel;
        this.pace = pace;
        this.goalWeight = goalWeight;
        this.calorieTarget = calorieTarget;
        this.user = user;
    }

    public Long getId(){
        return id;
    }

    public int getAge(){
        return age;
    }

    public double getWeight(){
        return weight;
    }

    public double getHeight(){
        return height;
    }

    public double getGoalWeight() { return goalWeight;}

    public Gender getGender(){
        return gender;
    }

    public ActivityLevel getActivityLevel(){ return activityLevel; }

    public Pace getPace(){return pace;}

    public double getCalorieTarget(){return calorieTarget;}

    public User getUser(){return user;}


    public void setId(Long id){
        this.id = id;
    }

    public void setAge(int age){
        this.age = age;
    }

    public void setWeight(double weight){
        this.weight = weight;
    }

    public void setHeight(double height){
        this.height = height;
    }

    public void setGoalWeight(double goalWeight){
        this.goalWeight = goalWeight;
    }

    public void setGender(Gender gender){
        this.gender = gender;
    }

    public void setActivityLevel(ActivityLevel activityLevel){
        this.activityLevel = activityLevel;
    }

    public void setPace(Pace pace){
        this.pace = pace;
    }

    public void setCalorieTarget(double calorieTarget){this.calorieTarget = calorieTarget;}

    public void setUser(User user){this.user = user;}
}
