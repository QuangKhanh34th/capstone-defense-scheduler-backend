package com.capstone.scheduler.repository;

import com.capstone.scheduler.entity.LecturerCompetency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LecturerCompetencyRepository extends JpaRepository<LecturerCompetency, Integer> {
    // Lấy điểm năng lực của GV đối với 1 vai trò (VD: Ông A làm Chủ tịch được mấy điểm?)
    Optional<LecturerCompetency> findByLecturer_LecturerIdAndCouncilRole_RoleId(Integer lecturerId, Integer roleId);
}