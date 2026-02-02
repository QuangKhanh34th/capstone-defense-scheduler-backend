package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.request.SetCompetencyRequest;
import com.capstone.scheduler.entity.CouncilRole;
import com.capstone.scheduler.entity.Lecturer;
import com.capstone.scheduler.entity.LecturerCompetency;
import com.capstone.scheduler.repository.CouncilRoleRepository;
import com.capstone.scheduler.repository.LecturerCompetencyRepository;
import com.capstone.scheduler.repository.LecturerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompetencyService {

    private final LecturerCompetencyRepository competencyRepository;
    private final LecturerRepository lecturerRepository;
    private final CouncilRoleRepository roleRepository;

    @Transactional
    public List<String> setCompetencies(Integer lecturerId, List<SetCompetencyRequest> requests) {

        Lecturer lecturer = lecturerRepository.findById(lecturerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Lecturer not found with ID: " + lecturerId));

        List<LecturerCompetency> toSave = new ArrayList<>();
        List<String> logs = new ArrayList<>();

        for (SetCompetencyRequest req : requests) {

            CouncilRole role = roleRepository.findById(req.getRoleId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Council Role not found with ID: " + req.getRoleId()));

            LecturerCompetency competency = competencyRepository
                    .findByLecturer_LecturerIdAndCouncilRole_RoleId(lecturerId, req.getRoleId())
                    .orElse(null);

            if (competency == null) {
                competency = LecturerCompetency.builder()
                        .lecturer(lecturer)
                        .councilRole(role)
                        .weight(req.getWeight())
                        .build();

                logs.add("Role [" + role.getRoleCode() + "] - Created weight: " + req.getWeight());
            } else {
                competency.setWeight(req.getWeight());

                logs.add("Role [" + role.getRoleCode() + "] - Updated weight: " + req.getWeight());
            }

            toSave.add(competency);
        }

        competencyRepository.saveAll(toSave);

        return logs;
    }
}