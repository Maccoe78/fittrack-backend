package com.mackessels.fittrackbackend.service;

import com.mackessels.fittrackbackend.dto.LoginRequestDTO;
import com.mackessels.fittrackbackend.dto.LoginResponseDTO;
import com.mackessels.fittrackbackend.dto.RegisterRequestDTO;
import com.mackessels.fittrackbackend.dto.RegisterResponseDTO;
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

    public void deleteUser (Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found by id: " + id);
        }
        userRepository.deleteById(id);
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

}
