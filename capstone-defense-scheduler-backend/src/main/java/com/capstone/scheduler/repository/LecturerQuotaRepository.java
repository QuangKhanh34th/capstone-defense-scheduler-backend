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

    @Query("SELECT lq FROM LecturerQuota lq WHERE lq.defenseRound.roundId = :roundId")
    List<LecturerQuota> findByRoundId(@Param("roundId") Integer roundId);

    @Query("SELECT lq FROM LecturerQuota lq WHERE lq.lecturer.lecturerId = :lecturerId AND lq.defenseRound.roundId = :roundId")
    Optional<LecturerQuota> findByLecturerIdAndRoundId(@Param("lecturerId") Integer lecturerId, @Param("roundId") Integer roundId);
}
