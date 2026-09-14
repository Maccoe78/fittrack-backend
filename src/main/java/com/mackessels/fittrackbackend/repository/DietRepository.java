package com.mackessels.fittrackbackend.repository;

import com.mackessels.fittrackbackend.model.DietPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DietRepository extends JpaRepository<DietPlan, Long> {
    Long Id(Long id);

    List<DietPlan> id(Long id);
}
