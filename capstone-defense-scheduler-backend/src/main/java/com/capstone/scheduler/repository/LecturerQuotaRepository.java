package com.capstone.scheduler.repository;

import com.capstone.scheduler.entity.LecturerQuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LecturerQuotaRepository extends JpaRepository<LecturerQuota, Integer> {

    Optional<LecturerQuota> findByDefenseRound_RoundIdAndLecturer_LecturerId(Integer roundId, Integer lecturerId);
}