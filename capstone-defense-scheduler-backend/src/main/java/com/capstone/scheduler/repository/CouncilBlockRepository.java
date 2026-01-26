package com.capstone.scheduler.repository;

import com.capstone.scheduler.entity.CouncilBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouncilBlockRepository extends JpaRepository<CouncilBlock, Integer> {

    @Query("SELECT cb FROM CouncilBlock cb " +
           "JOIN cb.defenseDay dd " +
           "WHERE dd.defenseRound.roundId = :roundId " +
           "ORDER BY dd.defenseDate, cb.startTime")
    List<CouncilBlock> findByRoundId(@Param("roundId") Integer roundId);

    List<CouncilBlock> findByDefenseDay_DayIdOrderByStartTime(Integer dayId);
}
