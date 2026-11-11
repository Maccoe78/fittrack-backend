package com.mackessels.fittrackbackend.integration;

import com.mackessels.fittrackbackend.model.User;
import com.mackessels.fittrackbackend.repository.UserRepository;
import com.mackessels.fittrackbackend.model.ActivityLevel;
import com.mackessels.fittrackbackend.model.DietPlan;
import com.mackessels.fittrackbackend.model.Gender;
import com.mackessels.fittrackbackend.model.Pace;
import com.mackessels.fittrackbackend.repository.DietRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.jayway.jsonpath.JsonPath;

import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DietRepository dietRepository;

    private RequestPostProcessor authenticatedAs(Long userId) {
        return jwt().jwt(token -> token.subject(userId.toString()));
    }

    @BeforeEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void createUser_returnsCreatedUser() throws Exception {
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "integration-user-1",
                            "password": "secret"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("integration-user-1"))
                .andExpect(jsonPath("$.password").doesNotExist());

        User savedUser = userRepository.findByName("integration-user-1")
                .orElseThrow();

        assertNotEquals("secret", savedUser.getPassword());
        assertTrue(passwordEncoder.matches("secret", savedUser.getPassword()));
    }

    @Test
    void getUserById_returnsCreatedUser() throws Exception {
        User savedUser = userRepository.save(new User(null, "integration-user-2", "secret"));

        mockMvc.perform(get("/users/{id}", savedUser.getId()).with(authenticatedAs(savedUser.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.name").value("integration-user-2"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void getAllUsers_returnsUsersWithoutPasswords() throws Exception {
        User savedUser = userRepository.save(
                new User(null, "integration-user", passwordEncoder.encode("secret"))
        );

        mockMvc.perform(get("/users").with(authenticatedAs(savedUser.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("integration-user"))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void createUser_withExistingName_returnsBadRequest() throws Exception {
        userRepository.save(new User(null, "duplicate-user", "secret"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "name": "duplicate-user",
                            "password": "secret"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
    void getUserById_unknownId_returnsNotFound() throws Exception {
        mockMvc.perform(get("/users/{id}", 999999).with(authenticatedAs(999999L)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteMyAccount_withWrongPasswordKeepsAccount() throws Exception {
        User user = userRepository.save(
                new User(null, "delete-user", passwordEncoder.encode("secret"))
        );

        mockMvc.perform(delete("/users/me")
                        .with(authenticatedAs(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "currentPassword": "wrong"
                            }
                            """))
                .andExpect(status().isForbidden());

        assertTrue(userRepository.findById(user.getId()).isPresent());
    }

    @Test
    void deleteMyAccount_withCorrectPasswordRemovesAccount() throws Exception {
        User user = userRepository.save(
                new User(null, "delete-user", passwordEncoder.encode("secret"))
        );

        mockMvc.perform(delete("/users/me")
                        .with(authenticatedAs(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "currentPassword": "secret"
                            }
                            """))
                .andExpect(status().isNoContent());

        assertTrue(userRepository.findById(user.getId()).isEmpty());
    }

    @Test
    void login_withValidCredentials_returnsIdAndName() throws Exception {
        User savedUser = userRepository.save(new User(null, "login-user", passwordEncoder.encode("secret")));

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "name": "login-user",
                            "password": "secret"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.name").value("login-user"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_withWrongPassword_returnsUnauthorized() throws Exception {
        userRepository.save(new User(null, "login-user", passwordEncoder.encode("secret")));

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "name": "login-user",
                            "password": "wrong"
                        }
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("invalid credentials"));
    }

    @Test
    void login_withUnknownUser_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "name": "does-not-exist",
                            "password": "secret"
                        }
                        """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMyProfile_returnsOwnProfileWithoutPassword() throws Exception {
        User user = userRepository.save(
                new User(null, "profile-user", passwordEncoder.encode("secret"))
        );

        mockMvc.perform(get("/users/me").with(authenticatedAs(user.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.name").value("profile-user"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void updateMyProfile_changesName() throws Exception {
        User user = userRepository.save(
                new User(null, "old-name", passwordEncoder.encode("secret"))
        );

        mockMvc.perform(put("/users/me")
                        .with(authenticatedAs(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "currentPassword": "secret",
                                "name": "new-name"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("new-name"))
                .andExpect(jsonPath("$.password").doesNotExist());

        assertTrue(userRepository.findById(user.getId())
                .orElseThrow().getName().equals("new-name"));
    }

    @Test
    void updateMyProfile_changesPasswordAndStoresItEncoded() throws Exception {
        User user = userRepository.save(
                new User(null, "password-user", passwordEncoder.encode("old-secret"))
        );

        mockMvc.perform(put("/users/me")
                        .with(authenticatedAs(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "currentPassword": "old-secret",
                                "newPassword": "new-secret"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist());

        String savedPassword = userRepository.findById(user.getId())
                .orElseThrow().getPassword();

        assertTrue(passwordEncoder.matches("new-secret", savedPassword));
    }

    @Test
    void updateMyProfile_withWrongCurrentPasswordReturnsForbidden() throws Exception {
        User user = userRepository.save(
                new User(null, "unchanged-name", passwordEncoder.encode("secret"))
        );

        mockMvc.perform(put("/users/me")
                        .with(authenticatedAs(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "currentPassword": "wrong",
                                "name": "changed-name"
                            }
                            """))
                .andExpect(status().isForbidden());

        assertTrue(userRepository.findById(user.getId())
                .orElseThrow().getName().equals("unchanged-name"));
    }

    @Test
    void deleteMyAccount_alsoRemovesOwnDietPlans() throws Exception {
        User user = userRepository.save(
                new User(null, "delete-user", passwordEncoder.encode("secret"))
        );
        User otherUser = userRepository.save(
                new User(null, "other-user", passwordEncoder.encode("other-secret"))
        );

        DietPlan ownPlan = dietRepository.save(new DietPlan(
                null, 25, 80, 180, 75,
                Gender.MALE, ActivityLevel.MODERATE, Pace.NORMAL,
                2200, user
        ));
        DietPlan otherPlan = dietRepository.save(new DietPlan(
                null, 30, 70, 170, 65,
                Gender.FEMALE, ActivityLevel.LIGHT, Pace.SLOW,
                1900, otherUser
        ));

        mockMvc.perform(delete("/users/me")
                        .with(authenticatedAs(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "currentPassword": "secret"
                            }
                            """))
                .andExpect(status().isNoContent());

        assertTrue(userRepository.findById(user.getId()).isEmpty());
        assertTrue(dietRepository.findById(ownPlan.getId()).isEmpty());

        assertTrue(userRepository.findById(otherUser.getId()).isPresent());
        assertTrue(dietRepository.findById(otherPlan.getId()).isPresent());
    }

    @Test
    void loginToken_canAccessOwnProfile() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "jwt-user",
                                "password": "secret"
                            }
                            """))
                .andExpect(status().isCreated());

        String loginResponse = mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "jwt-user",
                                "password": "secret"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = JsonPath.read(loginResponse, "$.token");

        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("jwt-user"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void getMyProfile_withInvalidTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }
}