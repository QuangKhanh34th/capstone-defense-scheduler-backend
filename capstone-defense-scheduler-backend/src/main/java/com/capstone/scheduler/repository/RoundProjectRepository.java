package com.capstone.scheduler.repository;

import com.capstone.scheduler.entity.RoundProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoundProjectRepository extends JpaRepository<RoundProject, Integer> {

    /**
     * Find all round projects with eagerly loaded Project and Council (with CouncilBlock)
     */
    @Query("SELECT rp FROM RoundProject rp " +
           "JOIN FETCH rp.project " +
           "LEFT JOIN FETCH rp.council c " +
           "LEFT JOIN FETCH c.councilBlock " +
           "WHERE rp.defenseRound.roundId = :roundId")
    List<RoundProject> findByRoundId(@Param("roundId") Integer roundId);

    @Query("SELECT rp FROM RoundProject rp WHERE rp.council.councilId = :councilId")
    List<RoundProject> findByCouncilId(@Param("councilId") Integer councilId);
}
