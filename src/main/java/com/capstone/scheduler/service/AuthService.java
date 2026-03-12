package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.request.CreateUserRequest;
import com.capstone.scheduler.dto.request.LoginRequest;
import com.capstone.scheduler.dto.request.RefreshTokenRequest;
import com.capstone.scheduler.dto.response.LoginResponse;
import com.capstone.scheduler.dto.response.UserResponse;
import com.capstone.scheduler.exception.RefreshTokenExpiredException;
import com.capstone.scheduler.entity.RefreshToken;
import com.capstone.scheduler.entity.User;
import com.capstone.scheduler.enums.CommonStatus; // IMPORT ENUM
import com.capstone.scheduler.enums.UserRole;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        User user = userDetailsService.loadUserEntityByUsername(request.getUsername());

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getUserId());
        extraClaims.put("role", user.getRole().name());
        String accessToken = jwtService.generateAccessToken(extraClaims, userDetails);
        String refreshTokenString = jwtService.generateRefreshToken(userDetails);

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
                .lecturerId(user.getLecturer() != null ? user.getLecturer().getLecturerId() : null)
                .build();
    }

    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RefreshTokenExpiredException(request.getRefreshToken(), "Refresh token is not in database!"));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new RefreshTokenExpiredException(refreshToken.getToken(), "Refresh token was expired. Please make a new signin request");
        }
        
        if (refreshToken.isRevoked()) {
            throw new BadCredentialsException("Refresh token is revoked");
        }

        User user = refreshToken.getUser();
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getUserId());
        extraClaims.put("role", user.getRole().name());
        String newAccessToken = jwtService.generateAccessToken(extraClaims, userDetails);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken())
                .username(user.getUsername())
                .role(user.getRole())
                .expiresIn(jwtService.getAccessTokenExpiration())
                .lecturerId(user.getLecturer() != null ? user.getLecturer().getLecturerId() : null)
                .build();
    }

    @Transactional
    public void logout(String username) {
        User user = userDetailsService.loadUserEntityByUsername(username);
        refreshTokenRepository.revokeAllByUser(user);
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }

        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .status(CommonStatus.ACTIVE) // FIXED: Dùng Enum
                .build();

        user = userRepository.save(user);

        return UserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }

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