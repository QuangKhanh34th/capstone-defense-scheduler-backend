package com.capstone.scheduler.repository;

import com.capstone.scheduler.entity.LecturerAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LecturerAvailabilityRepository extends JpaRepository<LecturerAvailability, Integer> {

    /**
     * Find all availabilities for a round with eagerly loaded Lecturer
     */
    @Query("SELECT la FROM LecturerAvailability la " +
           "JOIN FETCH la.lecturer " +
           "WHERE la.defenseRound.roundId = :roundId " +
           "AND la.lecturer.status = 'ACTIVE'")
    List<LecturerAvailability> findByRoundId(@Param("roundId") Integer roundId);

    @Query("SELECT la FROM LecturerAvailability la " +
           "JOIN FETCH la.lecturer " +
           "JOIN FETCH la.defenseRound " +
           "WHERE la.lecturer.lecturerId = :lecturerId " +
           "AND la.defenseRound.roundId = :roundId " +
           "AND la.lecturer.status = 'ACTIVE'")
    List<LecturerAvailability> findByLecturerIdAndRoundId(@Param("lecturerId") Integer lecturerId, @Param("roundId") Integer roundId);

    boolean existsByLecturer_LecturerIdAndAvailableDate(Integer lecturerId, LocalDate date);

    @Query("SELECT la.availableDate, COUNT(la) " +
            "FROM LecturerAvailability la " +
            "WHERE la.defenseRound.roundId = :roundId " +
            "AND la.lecturer.status = 'ACTIVE' " +
            "GROUP BY la.availableDate " +
            "ORDER BY la.availableDate ASC")
    List<Object[]> countLecturersByDate(@Param("roundId") Integer roundId);
}
