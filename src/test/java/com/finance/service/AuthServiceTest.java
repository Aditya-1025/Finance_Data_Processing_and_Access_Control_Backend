package com.finance.service;

import com.finance.dto.auth.AuthResponse;
import com.finance.dto.auth.LoginRequest;
import com.finance.dto.auth.RegisterRequest;
import com.finance.entity.User;
import com.finance.enums.Role;
import com.finance.enums.UserStatus;
import com.finance.exception.AppException;
import com.finance.repository.UserRepository;
import com.finance.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterSuccess() {
        RegisterRequest req = new RegisterRequest("Test User", "test@finance.dev", "secret");
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        User saved = User.builder()
                .id(5L)
                .name("Test User")
                .email("test@finance.dev")
                .passwordHash("encoded")
                .build();
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("jwt-token");
        AuthResponse resp = authService.register(req);
        assertNotNull(resp);
        assertEquals("jwt-token", resp.token());
        assertEquals(5L, resp.userId());
    }

    @Test
    void testRegisterDuplicateEmail() {
        RegisterRequest req = new RegisterRequest("Dup User", "dup@finance.dev", "pwd");
        when(userRepository.existsByEmail(any())).thenReturn(true);
        AppException ex = assertThrows(AppException.class, () -> authService.register(req));
        assertEquals("Email already registered", ex.getMessage());
    }

    @Test
    void testLoginSuccess() {
        LoginRequest req = new LoginRequest("admin@finance.dev", "password123");
        User user = User.builder()
                .id(1L)
                .email("admin@finance.dev")
                .passwordHash("encoded")
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("admin-jwt");
        AuthResponse resp = authService.login(req);
        assertEquals("admin-jwt", resp.token());
        assertEquals(1L, resp.userId());
    }

    @Test
    void testLoginInvalidPassword() {
        LoginRequest req = new LoginRequest("admin@finance.dev", "wrong");
        User user = User.builder()
                .passwordHash("encoded")
                .status(UserStatus.ACTIVE)
                .email("admin@finance.dev")
                .name("Admin")
                .build();
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);
        AppException ex = assertThrows(AppException.class, () -> authService.login(req));
        assertEquals("Invalid email or password", ex.getMessage());
    }
}
