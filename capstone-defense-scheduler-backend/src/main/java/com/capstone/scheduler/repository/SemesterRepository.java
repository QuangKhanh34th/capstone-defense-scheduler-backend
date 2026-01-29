package com.capstone.scheduler.repository;

import com.capstone.scheduler.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, Integer> {
    // Tìm học kỳ đang active (nếu có logic kích hoạt học kỳ)
    Optional<Semester> findByStatus(String status);
}