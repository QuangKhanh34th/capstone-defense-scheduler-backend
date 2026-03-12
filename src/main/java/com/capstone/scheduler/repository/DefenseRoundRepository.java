package com.capstone.scheduler.repository;

import com.capstone.scheduler.entity.DefenseRound;
import com.capstone.scheduler.enums.RoundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DefenseRoundRepository extends JpaRepository<DefenseRound, Integer>, JpaSpecificationExecutor<DefenseRound> {
    // Tìm các đợt bảo vệ theo học kỳ
    List<DefenseRound> findBySemester_SemesterId(Integer semesterId);
    List<DefenseRound> findByStatus(RoundStatus status);
    @Query("SELECT r FROM DefenseRound r WHERE r.status = 'ON_GOING' " +
            "AND (SELECT MAX(d.defenseDate) FROM DefenseDay d WHERE d.defenseRound = r) < CURRENT_DATE")
    List<DefenseRound> findRoundsReadyToComplete();
}