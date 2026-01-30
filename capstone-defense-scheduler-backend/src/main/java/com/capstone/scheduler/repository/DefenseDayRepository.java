package com.capstone.scheduler.repository;

import com.capstone.scheduler.entity.DefenseDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DefenseDayRepository extends JpaRepository<DefenseDay, Integer> {
    // Lấy tất cả ngày bảo vệ của 1 đợt (sắp xếp tăng dần theo thời gian)
    List<DefenseDay> findByDefenseRound_RoundIdOrderByDefenseDateAsc(Integer roundId);
    boolean existsByDefenseRound_RoundIdAndDefenseDate(Integer roundId, LocalDate defenseDate);
}