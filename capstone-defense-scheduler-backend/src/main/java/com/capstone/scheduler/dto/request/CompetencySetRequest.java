package com.capstone.scheduler.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CompetencySetRequest {

    @NotEmpty(message = "Danh sách năng lực không được rỗng")
    @Valid
    private List<RoleScoreDTO> competencies;

    @Data
    public static class RoleScoreDTO {
        @NotNull(message = "Role ID là bắt buộc")
        private Integer roleId;

        // BR-21: Matrix 0-5
        @NotNull(message = "Score is required")
        @Min(value = 0, message = "Score min is 0")
        @Max(value = 5, message = "Score max is 5")
        private Double score;
    }
}