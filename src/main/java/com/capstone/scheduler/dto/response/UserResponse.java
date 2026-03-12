package com.capstone.scheduler.dto.response;

import com.capstone.scheduler.enums.CommonStatus;
import com.capstone.scheduler.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Integer userId;
    private String username;
    private UserRole role;
    private CommonStatus status;
    private Integer lecturerId;
}
