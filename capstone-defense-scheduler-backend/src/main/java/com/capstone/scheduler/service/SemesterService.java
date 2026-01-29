package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.request.CreateSemesterRequest;
import com.capstone.scheduler.dto.response.SemesterResponse;
import com.capstone.scheduler.entity.Semester;
import com.capstone.scheduler.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class SemesterService {

    private final SemesterRepository semesterRepository;

    @Transactional
    public SemesterResponse createSemester(CreateSemesterRequest request) {
        // Validate Logic: Ngày kết thúc phải sau ngày bắt đầu
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be after start date");
        }

        // Validate Logic: Trùng tên học kỳ
        if (semesterRepository.existsByName(request.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Semester with name '" + request.getName() + "' already exists");
        }

        // Map DTO -> Entity
        Semester semester = Semester.builder()
                .name(request.getName())
                .schoolYear(request.getSchoolYear())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status("UPCOMING")
                .build();

        // Save DB
        Semester savedSemester = semesterRepository.save(semester);

        // Map Entity -> Response
        return SemesterResponse.builder()
                .semesterId(savedSemester.getSemesterId())
                .name(savedSemester.getName())
                .schoolYear(savedSemester.getSchoolYear())
                .startDate(savedSemester.getStartDate())
                .endDate(savedSemester.getEndDate())
                .status(savedSemester.getStatus())
                .build();
    }
}