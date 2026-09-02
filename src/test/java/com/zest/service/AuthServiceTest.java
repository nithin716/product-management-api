package com.zest.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.zest.dto.auth.AuthResponse;
import com.zest.dto.auth.LoginRequest;
import com.zest.dto.auth.RegisterRequest;
import com.zest.entity.RefreshToken;
import com.zest.entity.Role;
import com.zest.entity.User;
import com.zest.exception.InvalidAuthenticationException;
import com.zest.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    private User user;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {

        user = new User();

        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("encodedPassword");
        user.setRole(Role.ADMIN);

        refreshToken = new RefreshToken();

        refreshToken.setId(1L);
        refreshToken.setToken("refresh-token");
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(
                LocalDateTime.now().plusDays(7)
        );
        refreshToken.setRevoked(false);
    }

    @Test
    void register_shouldRegisterUserSuccessfully() {

        RegisterRequest request =
                new RegisterRequest(
                        "newuser",
                        "password123"
                );

        when(userRepository.existsByUsername("newuser"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        authService.register(request);

        verify(userRepository, times(1))
                .existsByUsername("newuser");

        verify(passwordEncoder, times(1))
                .encode("password123");

        verify(userRepository, times(1))
                .save(any(User.class));
    }

    @Test
    void register_shouldThrowExceptionWhenUsernameExists() {

        RegisterRequest request =
                new RegisterRequest(
                        "admin",
                        "password123"
                );

        when(userRepository.existsByUsername("admin"))
                .thenReturn(true);

        assertThrows(
                InvalidAuthenticationException.class,
                () -> authService.register(request)
        );

        verify(userRepository, times(1))
                .existsByUsername("admin");

        verify(userRepository, never())
                .save(any(User.class));

        verify(passwordEncoder, never())
                .encode(any(String.class));
    }

    @Test
    void login_shouldReturnTokensForValidCredentials() {

        LoginRequest request =
                new LoginRequest(
                        "admin",
                        "admin123"
                );

        when(userRepository.findByUsername("admin"))
                .thenReturn(java.util.Optional.of(user));

        when(passwordEncoder.matches(
                "admin123",
                "encodedPassword"
        )).thenReturn(true);

        when(jwtService.generateAccessToken(user))
                .thenReturn("access-token");

        when(refreshTokenService.createRefreshToken(user))
                .thenReturn(refreshToken);

        AuthResponse response =
                authService.login(request);

        assertEquals(
                "access-token",
                response.getAccessToken()
        );

        assertEquals(
                "refresh-token",
                response.getRefreshToken()
        );

        assertEquals(
                "Bearer",
                response.getTokenType()
        );

        verify(userRepository, times(1))
                .findByUsername("admin");

        verify(passwordEncoder, times(1))
                .matches(
                        "admin123",
                        "encodedPassword"
                );

        verify(jwtService, times(1))
                .generateAccessToken(user);

        verify(refreshTokenService, times(1))
                .createRefreshToken(user);
    }

    @Test
    void login_shouldThrowExceptionForWrongPassword() {

        LoginRequest request =
                new LoginRequest(
                        "admin",
                        "wrongPassword"
                );

        when(userRepository.findByUsername("admin"))
                .thenReturn(java.util.Optional.of(user));

        when(passwordEncoder.matches(
                "wrongPassword",
                "encodedPassword"
        )).thenReturn(false);

        assertThrows(
                InvalidAuthenticationException.class,
                () -> authService.login(request)
        );

        verify(userRepository, times(1))
                .findByUsername("admin");

        verify(passwordEncoder, times(1))
                .matches(
                        "wrongPassword",
                        "encodedPassword"
                );

        verify(jwtService, never())
                .generateAccessToken(any(User.class));

        verify(refreshTokenService, never())
                .createRefreshToken(any(User.class));
    }

    @Test
    void login_shouldThrowExceptionForUnknownUsername() {

        LoginRequest request =
                new LoginRequest(
                        "unknown",
                        "password123"
                );

        when(userRepository.findByUsername("unknown"))
                .thenReturn(java.util.Optional.empty());

        assertThrows(
                InvalidAuthenticationException.class,
                () -> authService.login(request)
        );

        verify(userRepository, times(1))
                .findByUsername("unknown");

        verify(passwordEncoder, never())
                .matches(
                        any(String.class),
                        any(String.class)
                );
    }

    @Test
    void refreshAccessToken_shouldRotateRefreshToken() {

        when(refreshTokenService.verifyRefreshToken(
                "old-refresh-token"
        )).thenReturn(refreshToken);

        when(jwtService.generateAccessToken(user))
                .thenReturn("new-access-token");

        RefreshToken newRefreshToken =
                new RefreshToken();

        newRefreshToken.setToken(
                "new-refresh-token"
        );

        newRefreshToken.setUser(user);

        when(refreshTokenService.createRefreshToken(user))
                .thenReturn(newRefreshToken);

        AuthResponse response =
                authService.refreshAccessToken(
                        "old-refresh-token"
                );

        assertEquals(
                "new-access-token",
                response.getAccessToken()
        );

        assertEquals(
                "new-refresh-token",
                response.getRefreshToken()
        );

        assertEquals(
                "Bearer",
                response.getTokenType()
        );

        verify(refreshTokenService, times(1))
                .verifyRefreshToken(
                        "old-refresh-token"
                );

        verify(refreshTokenService, times(1))
                .revokeRefreshToken(refreshToken);

        verify(jwtService, times(1))
                .generateAccessToken(user);

        verify(refreshTokenService, times(1))
                .createRefreshToken(user);
    }

    @Test
    void refreshAccessToken_shouldPropagateInvalidTokenException() {

        when(refreshTokenService.verifyRefreshToken(
                "invalid-token"
        )).thenThrow(
                new IllegalArgumentException(
                        "Invalid refresh token"
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.refreshAccessToken(
                        "invalid-token"
                )
        );

        verify(refreshTokenService, times(1))
                .verifyRefreshToken("invalid-token");

        verify(jwtService, never())
                .generateAccessToken(any(User.class));

        verify(refreshTokenService, never())
                .revokeRefreshToken(any(RefreshToken.class));
    }
}