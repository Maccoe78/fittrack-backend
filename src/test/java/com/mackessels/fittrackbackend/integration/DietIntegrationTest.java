package com.mackessels.fittrackbackend.integration;

import com.mackessels.fittrackbackend.model.ActivityLevel;
import com.mackessels.fittrackbackend.model.DietPlan;
import com.mackessels.fittrackbackend.model.Gender;
import com.mackessels.fittrackbackend.model.Pace;
import com.mackessels.fittrackbackend.model.User;
import com.mackessels.fittrackbackend.repository.DietRepository;
import com.mackessels.fittrackbackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DietIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DietRepository dietRepository;

    private User user;
    private User otherUser;
    private DietPlan ownPlan;
    private DietPlan otherPlan;

    private RequestPostProcessor authenticatedAs(Long userId) {
        return jwt().jwt(token -> token.subject(userId.toString()));
    }

    @BeforeEach
    void setUp() {
        dietRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(new User(null, "diet-user", "password"));
        otherUser = userRepository.save(new User(null, "other-user", "password"));

        ownPlan = dietRepository.save(new DietPlan(
                null, 25, 80, 180, 75,
                Gender.MALE, ActivityLevel.MODERATE, Pace.NORMAL,
                2200, user
        ));

        otherPlan = dietRepository.save(new DietPlan(
                null, 30, 70, 170, 65,
                Gender.FEMALE, ActivityLevel.LIGHT, Pace.SLOW,
                1900, otherUser
        ));
    }

    @Test
    void getAllDietPlans_returnsOnlyOwnPlans() throws Exception {
        mockMvc.perform(get("/diets").with(authenticatedAs(user.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(ownPlan.getId()))
                .andExpect(jsonPath("$[0].userId").value(user.getId()));
    }

    @Test
    void getDietPlanById_returnsOwnPlan() throws Exception {
        mockMvc.perform(get("/diets/{id}", ownPlan.getId())
                        .with(authenticatedAs(user.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ownPlan.getId()))
                .andExpect(jsonPath("$.userId").value(user.getId()));
    }

    @Test
    void getDietPlanById_fromOtherUserReturnsNotFound() throws Exception {
        mockMvc.perform(get("/diets/{id}", otherPlan.getId())
                        .with(authenticatedAs(user.getId())))
                .andExpect(status().isNotFound());
    }

    @Test
    void getDietPlanByUserId_fromOtherUserReturnsNotFound() throws Exception {
        mockMvc.perform(get("/diets/user/{userId}", otherUser.getId())
                        .with(authenticatedAs(user.getId())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteDietPlan_deletesOwnPlan() throws Exception {
        mockMvc.perform(delete("/diets/{id}", ownPlan.getId())
                        .with(authenticatedAs(user.getId())))
                .andExpect(status().isOk());

        assertTrue(dietRepository.findById(ownPlan.getId()).isEmpty());
        assertTrue(dietRepository.findById(otherPlan.getId()).isPresent());
    }

    @Test
    void deleteDietPlan_fromOtherUserReturnsNotFoundAndKeepsPlan() throws Exception {
        mockMvc.perform(delete("/diets/{id}", otherPlan.getId())
                        .with(authenticatedAs(user.getId())))
                .andExpect(status().isNotFound());

        assertTrue(dietRepository.findById(otherPlan.getId()).isPresent());
    }

    @Test
    void getDietPlans_withoutAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/diets"))
                .andExpect(status().isUnauthorized());
    }
}