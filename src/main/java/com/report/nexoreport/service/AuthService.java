package com.report.nexoreport.service;

import com.report.nexoreport.dto.AuthResponse;
import com.report.nexoreport.dto.LoginRequest;
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
    private final UserRepository userRepository;
    private final UserService userService;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserRepository userRepository,
            UserService userService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
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

        if (user.getStatus() == UserStatus.INVITED) {
            user.setStatus(UserStatus.ACTIVE);
            userRepository.save(user);
        }

        String token = jwtService.generateToken(
                user.getEmail(),
                Map.of("role", user.getRole().name(), "userId", user.getId())
        );
        UserResponse userResponse = userService.toUserResponse(user);
        return new AuthResponse(token, userResponse);
    }
}
