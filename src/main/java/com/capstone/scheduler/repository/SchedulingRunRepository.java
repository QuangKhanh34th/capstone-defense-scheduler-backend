package com.capstone.scheduler.repository;

import com.capstone.scheduler.entity.SchedulingRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SchedulingRunRepository extends JpaRepository<SchedulingRun, Integer> {
    // Lấy lịch sử chạy theo đợt, xếp giảm dần theo thời gian (mới nhất lên đầu)
    List<SchedulingRun> findByDefenseRound_RoundIdOrderByRunTimeDesc(Integer roundId);
}