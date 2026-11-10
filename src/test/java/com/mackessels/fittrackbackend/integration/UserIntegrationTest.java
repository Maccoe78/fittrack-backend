package com.mackessels.fittrackbackend.integration;

import com.mackessels.fittrackbackend.model.User;
import com.mackessels.fittrackbackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    void deleteUser_removesUser() throws Exception {
        User savedUser = userRepository.save(new User(null, "integration-user-3", "secret"));

        mockMvc.perform(delete("/users/{id}", savedUser.getId()).with(authenticatedAs(savedUser.getId())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/{id}", savedUser.getId()).with(authenticatedAs(savedUser.getId())))
                .andExpect(status().isNotFound());
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
    void deleteUser_unknownId_returnsNotFound() throws Exception {
        mockMvc.perform(delete("/users/{id}", 999999).with(authenticatedAs(999999L)))
                .andExpect(status().isNotFound());
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
}