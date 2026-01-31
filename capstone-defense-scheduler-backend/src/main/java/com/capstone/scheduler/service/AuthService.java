package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.request.CreateUserRequest;
import com.capstone.scheduler.dto.request.LoginRequest;
import com.capstone.scheduler.dto.request.RefreshTokenRequest;
import com.capstone.scheduler.dto.response.LoginResponse;
import com.capstone.scheduler.dto.response.UserResponse;
import com.capstone.scheduler.entity.RefreshToken;
import com.capstone.scheduler.entity.User;
import com.capstone.scheduler.repository.RefreshTokenRepository;
import com.capstone.scheduler.repository.UserRepository;
import com.capstone.scheduler.security.CustomUserDetailsService;
import com.capstone.scheduler.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    /**
     * Authenticate user and generate tokens
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Load user details
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        User user = userDetailsService.loadUserEntityByUsername(request.getUsername());

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshTokenString = jwtService.generateRefreshToken(userDetails);

        // Save refresh token to database
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenString)
                .user(user)
                .expiryDate(Instant.now().plusMillis(jwtService.getRefreshTokenExpiration()))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenString)
                .username(user.getUsername())
                .role(user.getRole())
                .expiresIn(jwtService.getAccessTokenExpiration())
                .build();
    }

    /**
     * Refresh access token using refresh token
     */
    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (!refreshToken.isValid()) {
            throw new BadCredentialsException("Refresh token is expired or revoked");
        }

        User user = refreshToken.getUser();
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

        // Generate new access token
        String newAccessToken = jwtService.generateAccessToken(userDetails);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken()) // Keep same refresh token
                .username(user.getUsername())
                .role(user.getRole())
                .expiresIn(jwtService.getAccessTokenExpiration())
                .build();
    }

    /**
     * Logout - revoke all refresh tokens for user
     */
    @Transactional
    public void logout(String username) {
        User user = userDetailsService.loadUserEntityByUsername(username);
        refreshTokenRepository.revokeAllByUser(user);
    }

    /**
     * Create new user (Admin only)
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }

        // Validate role
        String role = request.getRole().toUpperCase();
        if (!List.of("ADMIN", "LECTURER").contains(role)) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }

        // Create user
        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .status("ACTIVE")
                .build();

        user = userRepository.save(user);

        return UserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }

    /**
     * Get all users (Admin only)
     */
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> UserResponse.builder()
                        .userId(user.getUserId())
                        .username(user.getUsername())
                        .role(user.getRole())
                        .status(user.getStatus())
                        .build())
                .toList();
    }
}
