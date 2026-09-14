package com.mackessels.fittrackbackend.repository;

import com.mackessels.fittrackbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    Long Id(Long id);

    List<User> id(Long id);
}
