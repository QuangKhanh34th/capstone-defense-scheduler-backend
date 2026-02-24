package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.response.UserResponse;
import com.capstone.scheduler.entity.User;
import com.capstone.scheduler.repository.UserRepository;
import com.capstone.scheduler.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final CustomUserDetailsService userDetailsService;

    @Transactional(readOnly = true)
    public UserResponse getUserProfile(String username) {
        User user = userDetailsService.loadUserEntityByUsername(username);

        return UserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .role(user.getRole())
                .status(user.getStatus())
                .lecturerId(user.getLecturer() != null ? user.getLecturer().getLecturerId() : null)
                .build();
    }
}
