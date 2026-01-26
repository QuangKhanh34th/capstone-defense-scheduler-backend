package com.capstone.scheduler.repository;

import com.capstone.scheduler.entity.LecturerAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LecturerAvailabilityRepository extends JpaRepository<LecturerAvailability, Integer> {

    @Query("SELECT la FROM LecturerAvailability la WHERE la.defenseRound.roundId = :roundId")
    List<LecturerAvailability> findByRoundId(@Param("roundId") Integer roundId);

    @Query("SELECT la FROM LecturerAvailability la WHERE la.lecturer.lecturerId = :lecturerId AND la.defenseRound.roundId = :roundId")
    List<LecturerAvailability> findByLecturerIdAndRoundId(@Param("lecturerId") Integer lecturerId, @Param("roundId") Integer roundId);
}
