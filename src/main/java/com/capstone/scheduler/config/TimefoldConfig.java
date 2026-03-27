package com.capstone.scheduler.config;

import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;
import com.capstone.scheduler.solver.constraint.DefenseScheduleConstraintProvider;
import com.capstone.scheduler.solver.domain.DefenseScheduleSolution;
import com.capstone.scheduler.solver.domain.LecturerAssignment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Timefold Solver Configuration
 * Explicitly configures the solver to ensure proper termination and performance.
 */
@Configuration
public class TimefoldConfig {

    @Bean
    public SolverConfig solverConfig() {
        return new SolverConfig()
                .withSolutionClass(DefenseScheduleSolution.class)
                .withEntityClasses(LecturerAssignment.class)
                .withConstraintProviderClass(DefenseScheduleConstraintProvider.class)
                .withTerminationConfig(new TerminationConfig()
                        .withSpentLimit(Duration.ofSeconds(10))  // 10 seconds max
                        .withBestScoreLimit("0hard/0soft")      // Stop early if perfect hard score
                );
    }
}
