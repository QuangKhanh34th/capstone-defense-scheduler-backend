package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.request.LecturerImportRequest;
import com.capstone.scheduler.entity.Department;
import com.capstone.scheduler.entity.Lecturer;
import com.capstone.scheduler.entity.User;
import com.capstone.scheduler.repository.DepartmentRepository;
import com.capstone.scheduler.repository.LecturerRepository;
import com.capstone.scheduler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LecturerService {

    private final LecturerRepository lecturerRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    @Transactional(rollbackFor = Exception.class)
    public List<Lecturer> importLecturers(LecturerImportRequest request) {

        List<Lecturer> savedLecturers = new ArrayList<>();

        for (LecturerImportRequest.LecturerDTO dto : request.getLecturers()) {

            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Department ID not found: " + dto.getDepartmentId()));

            if (userRepository.existsByUsername(dto.getEmail())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "User account already exists for email: " + dto.getEmail());
            }

            if (lecturerRepository.existsByEmail(dto.getEmail())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Lecturer email already exists: " + dto.getEmail());
            }

            if (lecturerRepository.existsByLecturerCode(dto.getLecturerCode())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Lecturer Code already exists: " + dto.getLecturerCode());
            }

            if (dto.getPhone() != null && !dto.getPhone().isEmpty()) {
                if (lecturerRepository.existsByPhone(dto.getPhone())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Phone number already exists: " + dto.getPhone());
                }
            }

            User newUser = new User();
            newUser.setUsername(dto.getEmail());

            newUser.setPasswordHash("123456");

            newUser.setRole("LECTURER");

            newUser.setStatus("ACTIVE");

            User savedUser = userRepository.save(newUser);

            Lecturer lecturer = new Lecturer();
            lecturer.setLecturerCode(dto.getLecturerCode());
            lecturer.setFullName(dto.getFullName());
            lecturer.setEmail(dto.getEmail());
            lecturer.setPhone(dto.getPhone());
            lecturer.setIsActive(true);

            lecturer.setDepartment(department);
            lecturer.setUser(savedUser);

            savedLecturers.add(lecturerRepository.save(lecturer));
        }

        return savedLecturers;
    }
}