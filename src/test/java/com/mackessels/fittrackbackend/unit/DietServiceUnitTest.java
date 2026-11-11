package com.mackessels.fittrackbackend.unit;

import com.mackessels.fittrackbackend.dto.DietPlanRequestDTO;
import com.mackessels.fittrackbackend.model.ActivityLevel;
import com.mackessels.fittrackbackend.model.DietPlan;
import com.mackessels.fittrackbackend.model.Gender;
import com.mackessels.fittrackbackend.model.Pace;
import com.mackessels.fittrackbackend.model.User;
import com.mackessels.fittrackbackend.repository.DietRepository;
import com.mackessels.fittrackbackend.repository.UserRepository;
import com.mackessels.fittrackbackend.service.CalorieService;
import com.mackessels.fittrackbackend.service.DietService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DietServiceUnitTest {

    @Mock
    private DietRepository dietRepository;

    @Mock
    private CalorieService calorieService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DietService dietService;

    @Test
    void saveDietPlan_savesPlanForAuthenticatedUser() {
        User user = new User(1L, "test-user", "encoded-password");
        DietPlanRequestDTO request = new DietPlanRequestDTO();
        request.setAge(25);
        request.setWeight(80);
        request.setHeight(180);
        request.setGoalWeight(75);
        request.setGender(Gender.MALE);
        request.setActivityLevel(ActivityLevel.MODERATE);
        request.setPace(Pace.NORMAL);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        doAnswer(invocation -> {
            DietPlan plan = invocation.getArgument(0);
            plan.setCalorieTarget(2200);
            return null;
        }).when(calorieService).calculateCalories(any(DietPlan.class));

        when(dietRepository.save(any(DietPlan.class)))
                .thenAnswer(invocation -> {
                    DietPlan plan = invocation.getArgument(0);
                    plan.setId(10L);
                    return plan;
                });

        var result = dietService.saveDietPlan(request, 1L);

        assertEquals(10L, result.getId());
        assertEquals(1L, result.getUserId());
        assertEquals(25, result.getAge());
        assertEquals(2200, result.getCalorieTarget());
        verify(calorieService).calculateCalories(any(DietPlan.class));
        verify(dietRepository).save(any(DietPlan.class));
    }

    @Test
    void saveDietPlan_withUnknownUserReturnsNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> dietService.saveDietPlan(new DietPlanRequestDTO(), 999L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verifyNoInteractions(calorieService);
        verify(dietRepository, never()).save(any(DietPlan.class));
    }

    @Test
    void getAllDietPlans_returnsPlansForRequestedUser() {
        User user = new User(1L, "test-user", "encoded-password");
        DietPlan plan = new DietPlan();
        plan.setId(10L);
        plan.setUser(user);

        when(dietRepository.findAllByUser_Id(1L)).thenReturn(List.of(plan));

        var result = dietService.getAllDietPlans(1L);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
        assertEquals(1L, result.get(0).getUserId());
        verify(dietRepository).findAllByUser_Id(1L);
    }

    @Test
    void getDietPlanById_returnsOwnedPlan() {
        User user = new User(1L, "test-user", "encoded-password");
        DietPlan plan = new DietPlan();
        plan.setId(10L);
        plan.setUser(user);

        when(dietRepository.findByIdAndUser_Id(10L, 1L))
                .thenReturn(Optional.of(plan));

        var result = dietService.getDietPlanById(10L, 1L);

        assertEquals(10L, result.getId());
        assertEquals(1L, result.getUserId());
    }

    @Test
    void getDietPlanById_whenNotOwnedReturnsNotFound() {
        when(dietRepository.findByIdAndUser_Id(10L, 2L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> dietService.getDietPlanById(10L, 2L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void deleteDietPlan_deletesOwnedPlan() {
        DietPlan plan = new DietPlan();
        plan.setId(10L);

        when(dietRepository.findByIdAndUser_Id(10L, 1L))
                .thenReturn(Optional.of(plan));

        dietService.deleteDietPlan(10L, 1L);

        verify(dietRepository).delete(plan);
    }

    @Test
    void deleteDietPlan_whenNotOwnedDoesNotDelete() {
        when(dietRepository.findByIdAndUser_Id(10L, 2L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> dietService.deleteDietPlan(10L, 2L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(dietRepository, never()).delete(any(DietPlan.class));
    }
}