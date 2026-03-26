package com.capstone.scheduler.repository;

import com.capstone.scheduler.entity.CouncilBlockAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouncilBlockAssignmentRepository extends JpaRepository<CouncilBlockAssignment, Integer> {

    @Query("SELECT cba FROM CouncilBlockAssignment cba WHERE cba.councilBlock.blockId = :blockId")
    List<CouncilBlockAssignment> findByBlockId(@Param("blockId") Integer blockId);

    @Query("SELECT cba FROM CouncilBlockAssignment cba " +
           "JOIN cba.councilBlock cb " +
           "JOIN cb.defenseDay dd " +
           "WHERE dd.defenseRound.roundId = :roundId")
    List<CouncilBlockAssignment> findByRoundId(@Param("roundId") Integer roundId);

    @Query("SELECT cba FROM CouncilBlockAssignment cba WHERE cba.lecturer.lecturerId = :lecturerId")
    List<CouncilBlockAssignment> findByLecturerId(@Param("lecturerId") Integer lecturerId);

    @Query("SELECT cba FROM CouncilBlockAssignment cba " +
            "JOIN cba.councilBlock cb " +
            "JOIN cb.defenseDay dd " +
            "WHERE cba.lecturer.lecturerId = :lecturerId " +
            "AND dd.defenseRound.roundId = :roundId")
    List<CouncilBlockAssignment> findByLecturerIdAndRoundId(@Param("lecturerId") Integer lecturerId, @Param("roundId") Integer roundId);

    // Check xem GV đã có việc trong Ca này chưa
    boolean existsByCouncilBlock_BlockIdAndLecturer_LecturerId(Integer blockId, Integer lecturerId);
    void deleteByCouncilBlock_BlockIdIn(List<Integer> blockIds);
}

