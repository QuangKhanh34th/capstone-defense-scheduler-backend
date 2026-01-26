package com.capstone.scheduler.solver.domain;

import lombok.*;

/**
 * Immutable domain class representing council role information for the solver.
 * This is a problem fact.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouncilRoleInfo {

    private Integer roleId;
    private String roleCode;
    private String roleName;

    // Priority for role assignment (lower = assigned first during greedy assignment)
    private Integer priority;
}
