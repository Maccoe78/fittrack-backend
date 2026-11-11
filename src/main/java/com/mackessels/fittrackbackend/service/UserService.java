package com.mackessels.fittrackbackend.service;

import com.mackessels.fittrackbackend.dto.LoginRequestDTO;
import com.mackessels.fittrackbackend.dto.LoginResponseDTO;
import com.mackessels.fittrackbackend.dto.RegisterRequestDTO;
import com.mackessels.fittrackbackend.dto.RegisterResponseDTO;
import com.mackessels.fittrackbackend.dto.DeleteAccountRequestDTO;
import com.mackessels.fittrackbackend.dto.UpdateUserRequestDTO;
import com.mackessels.fittrackbackend.model.User;
import com.mackessels.fittrackbackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public RegisterResponseDTO saveUser(RegisterRequestDTO request){
        if(userRepository.findByName(request.getName()).isPresent()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);

        RegisterResponseDTO response = new RegisterResponseDTO();
        response.setId(savedUser.getId());
        response.setName(savedUser.getName());
        return response;
    }

    public List <User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id){
        return userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id= " + id));
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO){
        Optional<User> foundUser = userRepository.findByName(loginRequestDTO.getName());
        if (foundUser.isEmpty()){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials");
        }

        User user = foundUser.get();

        if (!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials");
        }

        LoginResponseDTO response = new LoginResponseDTO();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setToken(jwtService.createToken(user));

        return response;
    }

    public User updateUser(Long id, UpdateUserRequestDTO request) {
        User user = getUserById(id);
        verifyCurrentPassword(user, request.currentPassword());

        if (request.name() == null && request.newPassword() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No changes provided");
        }

        if (request.name() != null) {
            if (request.name().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name cannot be blank");
            }

            if (!request.name().equals(user.getName())
                    && userRepository.findByName(request.name()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
            }

            user.setName(request.name());
        }

        if (request.newPassword() != null) {
            if (request.newPassword().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password cannot be blank");
            }
            user.setPassword(passwordEncoder.encode(request.newPassword()));
        }

        return userRepository.save(user);
    }

    public void deleteUser(Long id, String currentPassword) {
        User user = getUserById(id);
        verifyCurrentPassword(user, currentPassword);
        userRepository.delete(user);
    }

    private void verifyCurrentPassword(User user, String currentPassword) {
        if (currentPassword == null
                || !passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Current password is incorrect");
        }
    }

}
