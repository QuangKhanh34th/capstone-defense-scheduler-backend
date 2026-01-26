package com.capstone.scheduler.repository;

import com.capstone.scheduler.entity.DefenseDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DefenseDayRepository extends JpaRepository<DefenseDay, Integer> {

    List<DefenseDay> findByDefenseRound_RoundId(Integer roundId);
}