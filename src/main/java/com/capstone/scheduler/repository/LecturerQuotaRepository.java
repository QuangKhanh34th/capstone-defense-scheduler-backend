package com.capstone.scheduler.repository;

import com.capstone.scheduler.entity.LecturerQuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LecturerQuotaRepository extends JpaRepository<LecturerQuota, Integer> {

    /**
     * Find all quotas for a round with eagerly loaded Lecturer
     */
    @Query("SELECT lq FROM LecturerQuota lq " +
           "JOIN FETCH lq.lecturer " +
           "WHERE lq.defenseRound.roundId = :roundId " +
           "AND lq.lecturer.status = 'ACTIVE'")
    List<LecturerQuota> findByRoundId(@Param("roundId") Integer roundId);

    @Query("SELECT lq FROM LecturerQuota lq " +
           "WHERE lq.lecturer.lecturerId = :lecturerId " +
           "AND lq.defenseRound.roundId = :roundId " +
           "AND lq.lecturer.status = 'ACTIVE'")
    Optional<LecturerQuota> findByLecturerIdAndRoundId(@Param("lecturerId") Integer lecturerId, @Param("roundId") Integer roundId);

    Optional<LecturerQuota> findByLecturer_LecturerIdAndDefenseRound_RoundId(Integer lecturerId, Integer roundId);
}
