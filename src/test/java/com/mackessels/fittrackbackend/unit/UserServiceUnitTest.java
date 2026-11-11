package com.mackessels.fittrackbackend.unit;

import com.mackessels.fittrackbackend.dto.LoginRequestDTO;
import com.mackessels.fittrackbackend.dto.RegisterRequestDTO;
import com.mackessels.fittrackbackend.dto.UpdateUserRequestDTO;
import com.mackessels.fittrackbackend.model.User;
import com.mackessels.fittrackbackend.repository.UserRepository;
import com.mackessels.fittrackbackend.service.JwtService;
import com.mackessels.fittrackbackend.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    @Test
    void saveUser_savesEncodedPasswordAndReturnsIdAndName() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setName("new-user");
        request.setPassword("secret");

        when(userRepository.findByName("new-user")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class)))
                .thenReturn(new User(7L, "new-user", "encoded-password"));

        var response = userService.saveUser(request);

        assertEquals(7L, response.getId());
        assertEquals("new-user", response.getName());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("new-user", userCaptor.getValue().getName());
        assertEquals("encoded-password", userCaptor.getValue().getPassword());

        verify(passwordEncoder).encode("secret");
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
    void deleteUser_deletesUserWhenCurrentPasswordIsCorrect() {
        User user = new User(1L, "test-user", "encoded-password");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("currentPassword", "encoded-password")).thenReturn(true);

        userService.deleteUser(1L, "currentPassword");

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_throwsNotFoundWhenUserIsMissing() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.deleteUser(999L, "currentPassword")
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(userRepository, never()).delete(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void deleteUser_doesNotDeleteWhenCurrentPasswordIsIncorrect() {
        User user = new User(1L, "test-user", "encoded-password");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encoded-password")).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.deleteUser(1L, "wrongPassword")
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void loginWithValidCredentialsReturnsIdAndName() {
        User user = new User(1L, "test-user", "encoded-password");
        when(userRepository.findByName("test-user"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "encoded-password"))
                .thenReturn(true);
        when(jwtService.createToken(user)).thenReturn("test-token");

        LoginRequestDTO request = new LoginRequestDTO();
        request.setName("test-user");
        request.setPassword("secret");

        var response = userService.login(request);

        assertEquals(1L, response.getId());
        assertEquals("test-user", response.getName());
        assertEquals("test-token", response.getToken());
    }

    @Test
    void loginWithUnknownUserThrowsUnauthorized() {
        when(userRepository.findByName("missing-user"))
                .thenReturn(Optional.empty());

        LoginRequestDTO request = new LoginRequestDTO();
        request.setName("missing-user");
        request.setPassword("secret");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.login(request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verifyNoInteractions(jwtService);
    }

    @Test
    void loginWithWrongPasswordThrowsUnauthorized() {
        User user = new User(1L, "test-user", "encoded-password");
        when(userRepository.findByName("test-user"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password"))
                .thenReturn(false);

        LoginRequestDTO request = new LoginRequestDTO();
        request.setName("test-user");
        request.setPassword("wrong-password");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.login(request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verifyNoInteractions(jwtService);
    }

    @Test
    void updateUser_changesNameWhenCurrentPasswordIsCorrect() {
        User user = new User(1L, "old-name", "encoded-password");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("currentPassword", "encoded-password")).thenReturn(true);
        when(userRepository.findByName("new-name")).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);

        var result = userService.updateUser(
                1L,
                new UpdateUserRequestDTO("currentPassword", "new-name", null)
        );

        assertEquals("new-name", result.getName());
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_changesPasswordWhenCurrentPasswordIsCorrect() {
        User user = new User(1L, "test-user", "encoded-old-password");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("currentPassword", "encoded-old-password"))
                .thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encoded-new-password");
        when(userRepository.save(user)).thenReturn(user);

        userService.updateUser(
                1L,
                new UpdateUserRequestDTO("currentPassword", null, "newPassword")
        );

        assertEquals("encoded-new-password", user.getPassword());
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_doesNotSaveWhenCurrentPasswordIsIncorrect() {
        User user = new User(1L, "test-user", "encoded-password");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encoded-password"))
                .thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.updateUser(
                        1L,
                        new UpdateUserRequestDTO("wrongPassword", "new-name", null)
                )
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(userRepository, never()).save(any(User.class));
    }
}