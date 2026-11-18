package com.mackessels.fittrackbackend.unit;

import com.mackessels.fittrackbackend.dto.FoodEntryRequestDTO;
import com.mackessels.fittrackbackend.model.DietPlan;
import com.mackessels.fittrackbackend.model.FoodEntry;
import com.mackessels.fittrackbackend.model.User;
import com.mackessels.fittrackbackend.repository.DietRepository;
import com.mackessels.fittrackbackend.repository.FoodEntryRepository;
import com.mackessels.fittrackbackend.repository.UserRepository;
import com.mackessels.fittrackbackend.service.FoodEntryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FoodEntryServiceUnitTest {

    @Mock
    private FoodEntryRepository foodEntryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DietRepository dietRepository;

    @InjectMocks
    private FoodEntryService foodEntryService;

    @Test
    void addFoodEntry_savesEntryForUserWithTodayDate() {
        User user = new User(1L, "test-user", "encoded-password");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(foodEntryRepository.save(any(FoodEntry.class)))
                .thenAnswer(invocation -> {
                    FoodEntry entry = invocation.getArgument(0);
                    entry.setId(10L);
                    return entry;
                });

        var result = foodEntryService.addFoodEntry(
                new FoodEntryRequestDTO(" Apple ", 60), 1L
        );

        assertEquals(10L, result.id());
        assertEquals("Apple", result.foodName());
        assertEquals(60, result.calories());
        assertEquals(LocalDate.now(ZoneId.of("Europe/Amsterdam")), result.logDate());

        var captor = org.mockito.ArgumentCaptor.forClass(FoodEntry.class);
        verify(foodEntryRepository).save(captor.capture());
        assertSame(user, captor.getValue().getUser());
    }

    @Test
    void addFoodEntry_whenUserDoesNotExistReturnsNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> foodEntryService.addFoodEntry(
                        new FoodEntryRequestDTO("Apple", 60), 999L
                )
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(foodEntryRepository, never()).save(any(FoodEntry.class));
    }

    @Test
    void getTodaySummary_calculatesConsumedAndRemainingCalories() {
        User user = new User(1L, "test-user", "encoded-password");
        FoodEntry apple = new FoodEntry();
        apple.setId(10L);
        apple.setUser(user);
        apple.setFoodName("Apple");
        apple.setCalories(60);
        apple.setLogDate(LocalDate.now(ZoneId.of("Europe/Amsterdam")));

        DietPlan plan = new DietPlan();
        plan.setCalorieTarget(3000);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(foodEntryRepository.findAllByUser_IdAndLogDate(
                eq(1L), any(LocalDate.class)))
                .thenReturn(List.of(apple));
        when(dietRepository.findByUser_Id(1L)).thenReturn(Optional.of(plan));

        var result = foodEntryService.getTodaySummary(1L);

        assertEquals(60L, result.consumedCalories());
        assertEquals(3000.0, result.calorieTarget());
        assertEquals(2940.0, result.remainingCalories());
        assertEquals(1, result.entries().size());
    }

    @Test
    void getTodaySummary_withoutDietStillReturnsConsumedCalories() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(foodEntryRepository.findAllByUser_IdAndLogDate(
                eq(1L), any(LocalDate.class)))
                .thenReturn(List.of());
        when(dietRepository.findByUser_Id(1L)).thenReturn(Optional.empty());

        var result = foodEntryService.getTodaySummary(1L);

        assertEquals(0L, result.consumedCalories());
        assertNull(result.calorieTarget());
        assertNull(result.remainingCalories());
    }
}