package com.mackessels.fittrackbackend.integration;

import com.mackessels.fittrackbackend.model.*;
import com.mackessels.fittrackbackend.repository.DietRepository;
import com.mackessels.fittrackbackend.repository.FoodEntryRepository;
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

import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FoodEntryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FoodEntryRepository foodEntryRepository;

    @Autowired
    private DietRepository dietRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private User otherUser;

    private RequestPostProcessor authenticatedAs(Long userId) {
        return jwt().jwt(token -> token.subject(userId.toString()));
    }

    @BeforeEach
    void setUp() {
        foodEntryRepository.deleteAll();
        dietRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(new User(null, "food-user", "password"));
        otherUser = userRepository.save(new User(null, "other-user", "password"));
    }

    private FoodEntry saveEntry(User owner, String name, int calories, LocalDate date) {
        FoodEntry entry = new FoodEntry();
        entry.setUser(owner);
        entry.setFoodName(name);
        entry.setCalories(calories);
        entry.setLogDate(date);
        return foodEntryRepository.save(entry);
    }

    @Test
    void addFoodEntry_savesEntryForAuthenticatedUser() throws Exception {
        mockMvc.perform(post("/food-entries")
                        .with(authenticatedAs(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "foodName": "Apple",
                                    "calories": 60
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.foodName").value("Apple"))
                .andExpect(jsonPath("$.calories").value(60))
                .andExpect(jsonPath("$.logDate").exists());

        assertEquals(1, foodEntryRepository
                .findAllByUser_IdAndLogDate(
                        user.getId(), LocalDate.now(ZoneId.of("Europe/Amsterdam")))
                .size());
    }

    @Test
    void getFoodEntries_returnsOnlyEntriesForUserAndDate() throws Exception {
        LocalDate date = LocalDate.of(2026, 11, 18);
        saveEntry(user, "Apple", 60, date);
        saveEntry(user, "Bread", 100, date.minusDays(1));
        saveEntry(otherUser, "Banana", 90, date);

        mockMvc.perform(get("/food-entries")
                        .param("date", date.toString())
                        .with(authenticatedAs(user.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].foodName").value("Apple"))
                .andExpect(jsonPath("$[0].calories").value(60));
    }

    @Test
    void addFoodEntry_withNegativeCaloriesReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/food-entries")
                        .with(authenticatedAs(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "foodName": "Apple",
                                    "calories": -10
                                }
                                """))
                .andExpect(status().isBadRequest());

        assertEquals(0, foodEntryRepository.count());
    }

    @Test
    void getFoodEntries_withoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/food-entries")
                        .param("date", "2026-11-18"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTodaySummary_calculatesRemainingCalories() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Amsterdam"));
        saveEntry(user, "Apple", 60, today);
        saveEntry(otherUser, "Banana", 90, today);

        dietRepository.save(new DietPlan(
                null, 25, 80, 180, 75,
                Gender.MALE, ActivityLevel.MODERATE, Pace.NORMAL,
                3000, user
        ));

        mockMvc.perform(get("/food-entries/today/summary")
                        .with(authenticatedAs(user.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consumedCalories").value(60))
                .andExpect(jsonPath("$.calorieTarget").value(3000.0))
                .andExpect(jsonPath("$.remainingCalories").value(2940.0))
                .andExpect(jsonPath("$.entries.length()").value(1));
    }

    @Test
    void getTodaySummary_withoutDietStillShowsConsumedCalories() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Amsterdam"));
        saveEntry(user, "Apple", 60, today);

        mockMvc.perform(get("/food-entries/today/summary")
                        .with(authenticatedAs(user.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consumedCalories").value(60))
                .andExpect(jsonPath("$.calorieTarget").value(
                        org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.remainingCalories").value(
                        org.hamcrest.Matchers.nullValue()));
    }
}