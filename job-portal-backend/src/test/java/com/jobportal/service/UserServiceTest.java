package com.jobportal.service;

import com.jobportal.dto.RegisterRequest;
import com.jobportal.exception.ApiException;
import com.jobportal.model.User;
import com.jobportal.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private NotificationEmailService notificationEmailService;

    @InjectMocks
    private UserService userService;

    @Test
    void registerThrowsWhenEmailExists() {
        RegisterRequest request = new RegisterRequest("John", "john@example.com", "secret123", "CANDIDATE");
        when(userRepository.findByEmailIgnoreCase("john@example.com")).thenReturn(Optional.of(new User()));

        ApiException exception = assertThrows(ApiException.class, () -> userService.register(request));
        assertEquals("User already exists with this email", exception.getMessage());
    }

    @Test
    void registerSavesEncodedPassword() {
        RegisterRequest request = new RegisterRequest("John", "john@example.com", "secret123", "CANDIDATE");
        when(userRepository.findByEmailIgnoreCase("john@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = userService.register(request);

        assertEquals("encoded", saved.getPassword());
        assertEquals("john@example.com", saved.getEmail());
    }
}
