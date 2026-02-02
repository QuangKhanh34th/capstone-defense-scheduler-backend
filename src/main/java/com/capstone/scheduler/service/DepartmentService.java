package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.response.DepartmentResponse;
import com.capstone.scheduler.entity.Department;
import com.capstone.scheduler.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        List<Department> departments = departmentRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));

        return departments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private DepartmentResponse mapToResponse(Department dept) {
        return DepartmentResponse.builder()
                .departmentId(dept.getDepartmentId())
                .name(dept.getName())
                .facultyName(dept.getFacultyName())
                .build();
    }
}