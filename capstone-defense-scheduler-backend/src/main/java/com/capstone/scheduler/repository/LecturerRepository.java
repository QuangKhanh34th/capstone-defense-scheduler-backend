package com.capstone.scheduler.repository;

import com.capstone.scheduler.entity.Lecturer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LecturerRepository extends JpaRepository<Lecturer, Integer> {
    // Check trùng các trường Unique
    boolean existsByLecturerCode(String lecturerCode);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

    // Tìm theo tên (để dùng cho chức năng Import Project)
    Optional<Lecturer> findByFullNameIgnoreCase(String fullName);
}