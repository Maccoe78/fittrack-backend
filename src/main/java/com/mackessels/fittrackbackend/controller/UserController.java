package com.mackessels.fittrackbackend.controller;

import com.mackessels.fittrackbackend.dto.*;
import com.mackessels.fittrackbackend.model.User;
import com.mackessels.fittrackbackend.service.UserService;
import com.mackessels.fittrackbackend.service.AuthenticatedUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final AuthenticatedUserService authenticatedUserService;

    public UserController(UserService userService, AuthenticatedUserService authenticatedUserService){
        this.userService = userService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @PostMapping
    public ResponseEntity<?> saveUser(@RequestBody RegisterRequestDTO requestDTO){
        try {
            RegisterResponseDTO response = userService.saveUser(requestDTO);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(Map.of("message", ex.getReason()));
        }
    }

    @GetMapping
    public List<UserResponseDTO> getAllUsers() {
        return userService.getAllUsers().stream().map(this::toUserResponse).toList();
    }

    @GetMapping("/{id}")
    public UserResponseDTO getUserById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt){
        authenticatedUserService.requireOwnUser(id, jwt);
        return toUserResponse(userService.getUserById(id));
    }

    private UserResponseDTO toUserResponse(User user) {
        return new UserResponseDTO(user.getId(), user.getName());
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        try {
            LoginResponseDTO response = userService.login(loginRequestDTO);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(Map.of("message", ex.getReason()));
        }

    }

    @GetMapping("/me")
    public UserResponseDTO getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        Long userId = authenticatedUserService.getUserId(jwt);
        return toUserResponse(userService.getUserById(userId));
    }

    @PutMapping("/me")
    public UserResponseDTO updateMyProfile(@RequestBody UpdateUserRequestDTO request, @AuthenticationPrincipal Jwt jwt) {
        Long userId = authenticatedUserService.getUserId(jwt);
        return toUserResponse(userService.updateUser(userId, request));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(@RequestBody DeleteAccountRequestDTO request, @AuthenticationPrincipal Jwt jwt) {
        Long userId = authenticatedUserService.getUserId(jwt);
        userService.deleteUser(userId, request.currentPassword());
        return ResponseEntity.noContent().build();
    }
}
