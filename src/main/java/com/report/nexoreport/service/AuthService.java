package com.report.nexoreport.service;

import com.report.nexoreport.auth.RefreshToken;
import com.report.nexoreport.dto.AccessTokenResponse;
import com.report.nexoreport.dto.AuthResponse;
import com.report.nexoreport.dto.LoginRequest;
import com.report.nexoreport.exception.BadRequestException;
import com.report.nexoreport.dto.UserResponse;
import com.report.nexoreport.exception.ResourceNotFoundException;
import com.report.nexoreport.repository.UserRepository;
import com.report.nexoreport.security.JwtService;
import com.report.nexoreport.user.User;
import com.report.nexoreport.user.UserStatus;
import java.util.Map;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final UserService userService;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            UserRepository userRepository,
            UserService userService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getStatus() == UserStatus.DEACTIVATED) {
            throw new BadRequestException("Deactivated users cannot login");
        }

        if (user.getStatus() == UserStatus.INVITED) {
            user.setStatus(UserStatus.ACTIVE);
            userRepository.save(user);
        }

        String accessToken = jwtService.generateAccessToken(
                user.getEmail(),
                Map.of("role", user.getRole().name(), "userId", user.getId())
        );
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        UserResponse userResponse = userService.toUserResponse(user);
        return new AuthResponse(accessToken, refreshToken.getToken(), userResponse);
    }

    @Transactional
    public AccessTokenResponse refreshAccessToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(refreshTokenValue);
        User user = refreshToken.getUser();
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("User account is not active");
        }
        String accessToken = jwtService.generateAccessToken(
                user.getEmail(),
                Map.of("role", user.getRole().name(), "userId", user.getId())
        );
        return new AccessTokenResponse(accessToken);
    }

    @Transactional
    public void logout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        refreshTokenService.deleteUserRefreshTokens(user);
    }
}
