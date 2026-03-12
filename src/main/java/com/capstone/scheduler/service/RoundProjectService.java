package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.request.AddProjectToRoundRequest;
import com.capstone.scheduler.entity.DefenseRound;
import com.capstone.scheduler.entity.Project;
import com.capstone.scheduler.entity.RoundProject;
import com.capstone.scheduler.enums.ProjectStatus; // MỚI: Nhớ import cái này
import com.capstone.scheduler.enums.RoundProjectStatus;
import com.capstone.scheduler.repository.DefenseRoundRepository;
import com.capstone.scheduler.repository.ProjectRepository;
import com.capstone.scheduler.repository.RoundProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoundProjectService {

    private final DefenseRoundRepository defenseRoundRepository;
    private final ProjectRepository projectRepository;
    private final RoundProjectRepository roundProjectRepository;

    @Transactional
    public List<String> addProjectsToRound(Integer roundId, AddProjectToRoundRequest request) {
        DefenseRound round = defenseRoundRepository.findById(roundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Defense Round not found with ID: " + roundId));

        Integer roundSemesterId = round.getSemester().getSemesterId();

        List<Project> projects = projectRepository.findAllById(request.getProjectIds());

        if (projects.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No projects found with provided IDs");
        }

        List<RoundProject> toSave = new ArrayList<>();
        List<String> results = new ArrayList<>();

        Set<Integer> existingProjectIds = roundProjectRepository
                .findByDefenseRound_RoundIdAndProject_ProjectIdIn(roundId, request.getProjectIds())
                .stream()
                .map(rp -> rp.getProject().getProjectId())
                .collect(Collectors.toSet());

        for (Project project : projects) {

            if (project.getStatus() != ProjectStatus.PENDING) {

                results.add("Project [" + project.getTitle() + "] - Skipped (Status is " + project.getStatus() + ", must be PENDING)");
                continue;
            }

            if (!project.getSemester().getSemesterId().equals(roundSemesterId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Project [" + project.getTitle() + "] belongs to a different Semester. Cannot add to this Round.");
            }

            if (existingProjectIds.contains(project.getProjectId())) {
                results.add("Project [" + project.getTitle() + "] - Skipped (Already in round)");
                continue;
            }

            RoundProject rp = RoundProject.builder()
                    .defenseRound(round)
                    .project(project)
                    .resultStatus(RoundProjectStatus.IN_PROGRESS)
                    .build();

            toSave.add(rp);
            results.add("Project [" + project.getTitle() + "] - Added successfully");
        }

        if (!toSave.isEmpty()) {
            roundProjectRepository.saveAll(toSave);
        }

        return results;
    }
}