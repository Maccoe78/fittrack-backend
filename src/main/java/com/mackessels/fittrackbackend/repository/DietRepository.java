package com.mackessels.fittrackbackend.repository;

import com.mackessels.fittrackbackend.model.DietPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DietRepository extends JpaRepository<DietPlan, Long> {
    Optional<DietPlan> findByUser_Id(Long userId);
    List<DietPlan> findAllByUser_Id(Long userId);

    Optional<DietPlan> findByIdAndUser_Id(Long id, Long userId);
}
