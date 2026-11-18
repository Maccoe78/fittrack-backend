package com.mackessels.fittrackbackend.repository;

import com.mackessels.fittrackbackend.model.FoodEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface FoodEntryRepository extends JpaRepository<FoodEntry, Long> {

    List<FoodEntry> findAllByUser_IdAndLogDate(Long userId, LocalDate logDate);
}