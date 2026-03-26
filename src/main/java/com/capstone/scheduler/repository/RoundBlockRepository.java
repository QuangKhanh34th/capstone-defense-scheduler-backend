package com.capstone.scheduler.repository;

import com.capstone.scheduler.entity.RoundBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoundBlockRepository extends JpaRepository<RoundBlock, Integer> {

    // Tìm các Nhóm/Phòng thi nằm trong 1 Ca cụ thể (blockId)
    List<RoundBlock> findByCouncilBlock_BlockId(Integer blockId);
    Optional<RoundBlock> findFirstByCouncilBlock_BlockId(Integer blockId);
    void deleteByCouncilBlock_BlockIdIn(List<Integer> blockIds);
}