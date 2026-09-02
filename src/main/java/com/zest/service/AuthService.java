package com.zest.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zest.dto.auth.AuthResponse;
import com.zest.dto.auth.LoginRequest;
import com.zest.dto.auth.RegisterRequest;
import com.zest.entity.RefreshToken;
import com.zest.entity.Role;
import com.zest.entity.User;
import com.zest.exception.InvalidAuthenticationException;
import com.zest.repository.UserRepository;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public void register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new InvalidAuthenticationException(
                    "Username already exists");
        }

        User user = new User();

        user.setUsername(request.getUsername());

        user.setPassword(
                passwordEncoder.encode(request.getPassword()));

        user.setRole(Role.USER);

        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository
                .findByUsername(request.getUsername())
                .orElseThrow(() ->
                        new InvalidAuthenticationException(
                                "Invalid username or password"));

        boolean passwordMatches =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPassword());

        if (!passwordMatches) {
            throw new InvalidAuthenticationException(
                    "Invalid username or password");
        }

        String accessToken =
                jwtService.generateAccessToken(user);

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(user);

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                "Bearer"
        );
    }

    public AuthResponse refreshAccessToken(String token) {

        RefreshToken oldRefreshToken =
                refreshTokenService.verifyRefreshToken(token);

        User user = oldRefreshToken.getUser();

        // Revoke the old refresh token.
        refreshTokenService.revokeRefreshToken(
                oldRefreshToken);

        // Generate a new access token.
        String newAccessToken =
                jwtService.generateAccessToken(user);

        // Generate a new refresh token.
        RefreshToken newRefreshToken =
                refreshTokenService.createRefreshToken(user);

        return new AuthResponse(
                newAccessToken,
                newRefreshToken.getToken(),
                "Bearer"
        );
    }
}