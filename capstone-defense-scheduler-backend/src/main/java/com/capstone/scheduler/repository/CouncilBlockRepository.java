package com.capstone.scheduler.repository;

import com.capstone.scheduler.entity.CouncilBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouncilBlockRepository extends JpaRepository<CouncilBlock, Integer> {

    List<CouncilBlock> findByDefenseDay_DayIdOrderByStartTimeAsc(Integer dayId);
}