package com.capstone.scheduler.repository;

import com.capstone.scheduler.entity.DefenseRound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DefenseRoundRepository extends JpaRepository<DefenseRound, Integer> {
}