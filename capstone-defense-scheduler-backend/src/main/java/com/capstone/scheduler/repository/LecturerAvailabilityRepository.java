package com.capstone.scheduler.repository;

import com.capstone.scheduler.entity.LecturerAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface LecturerAvailabilityRepository extends JpaRepository<LecturerAvailability, Integer> {

    boolean existsByLecturer_LecturerIdAndDefenseRound_RoundIdAndAvailableDate(
            Integer lecturerId, Integer roundId, LocalDate availableDate);
}