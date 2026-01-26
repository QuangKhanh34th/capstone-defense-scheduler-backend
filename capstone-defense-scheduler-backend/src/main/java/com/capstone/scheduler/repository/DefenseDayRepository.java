package com.capstone.scheduler.repository;

import com.capstone.scheduler.entity.DefenseDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DefenseDayRepository extends JpaRepository<DefenseDay, Integer> {

    List<DefenseDay> findByDefenseRound_RoundId(Integer roundId);

    boolean existsByDefenseRound_RoundIdAndDefenseDate(Integer roundId, LocalDate date);
}