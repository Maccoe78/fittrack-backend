package com.mackessels.fittrackbackend.unit;

import com.mackessels.fittrackbackend.dto.LoginRequestDTO;
import com.mackessels.fittrackbackend.dto.RegisterRequestDTO;
import com.mackessels.fittrackbackend.model.User;
import com.mackessels.fittrackbackend.repository.UserRepository;
import com.mackessels.fittrackbackend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void saveUser_savesUserAndReturnsResponseWithoutPassword() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setName("new-user");
        request.setPassword("secret");

        when(userRepository.findByName("new-user")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class)))
                .thenReturn(new User(7L, "new-user", "secret"));

        var response = userService.saveUser(request);

        assertEquals(7L, response.getId());
        assertEquals("new-user", response.getName());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("new-user", userCaptor.getValue().getName());
        assertEquals("secret", userCaptor.getValue().getPassword());
    }

    @Test
    void saveUser_withExistingNameThrowsBadRequest() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setName("existing-user");
        request.setPassword("secret");

        when(userRepository.findByName("existing-user"))
                .thenReturn(Optional.of(new User(1L, "existing-user", "secret")));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.saveUser(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getAllUsersReturnsRepositoryResults() {
        List<User> users = List.of(new User(1L, "user-one", "secret"));
        when(userRepository.findAll()).thenReturn(users);

        assertEquals(users, userService.getAllUsers());
    }

    @Test
    void getUserByIdReturnsUserWhenFound() {
        User user = new User(1L, "test-user", "secret");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertSame(user, userService.getUserById(1L));
    }

    @Test
    void getUserByIdThrowsNotFoundWhenMissing() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.getUserById(999L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void deleteUserDeletesUserWhenItExists() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUserThrowsNotFoundWhenMissing() {
        when(userRepository.existsById(999L)).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.deleteUser(999L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void loginWithValidCredentialsReturnsIdAndName() {
        User user = new User(1L, "test-user", "secret");
        when(userRepository.findByName("test-user")).thenReturn(Optional.of(user));

        LoginRequestDTO request = new LoginRequestDTO();
        request.setName("test-user");
        request.setPassword("secret");

        var response = userService.login(request);

        assertEquals(1L, response.getId());
        assertEquals("test-user", response.getName());
    }

    @Test
    void loginWithUnknownUserThrowsUnauthorized() {
        when(userRepository.findByName("missing-user")).thenReturn(Optional.empty());

        LoginRequestDTO request = new LoginRequestDTO();
        request.setName("missing-user");
        request.setPassword("secret");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.login(request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void loginWithWrongPasswordThrowsUnauthorized() {
        when(userRepository.findByName("test-user"))
                .thenReturn(Optional.of(new User(1L, "test-user", "secret")));

        LoginRequestDTO request = new LoginRequestDTO();
        request.setName("test-user");
        request.setPassword("wrong-password");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.login(request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }
}