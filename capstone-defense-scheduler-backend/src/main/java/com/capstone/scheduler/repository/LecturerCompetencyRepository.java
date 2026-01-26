package com.capstone.scheduler.repository;

import com.capstone.scheduler.entity.LecturerCompetency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LecturerCompetencyRepository extends JpaRepository<LecturerCompetency, Integer> {

    Optional<LecturerCompetency> findByLecturer_LecturerIdAndCouncilRole_RoleId(Integer lecturerId, Integer roleId);
}