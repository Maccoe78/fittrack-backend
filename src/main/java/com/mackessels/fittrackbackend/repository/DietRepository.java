package com.mackessels.fittrackbackend.repository;

import com.mackessels.fittrackbackend.model.DietPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DietRepository extends JpaRepository<DietPlan, Long> {
    Optional<DietPlan> findByUser_Id(Long userId);
}
