package com.capstone.scheduler.repository;

import com.capstone.scheduler.entity.DefenseRound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DefenseRoundRepository extends JpaRepository<DefenseRound, Integer> {
    // Tìm các đợt bảo vệ theo học kỳ
    List<DefenseRound> findBySemester_SemesterId(Integer semesterId);
}