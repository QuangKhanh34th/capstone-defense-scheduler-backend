package com.capstone.scheduler.repository;

import com.capstone.scheduler.entity.RoundProject;
import com.capstone.scheduler.enums.ProjectStatus;
import com.capstone.scheduler.enums.RoundProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoundProjectRepository extends JpaRepository<RoundProject, Integer> {

    /**
     * Lấy danh sách Project đầy đủ thông tin (Fetch Join)
     */

    @Query("SELECT DISTINCT rp FROM RoundProject rp " +
            "JOIN FETCH rp.project " +
            "LEFT JOIN FETCH rp.roundBlocks rb " +
            "LEFT JOIN FETCH rb.councilBlock " +
            "WHERE rp.defenseRound.roundId = :roundId")
    List<RoundProject> findByRoundId(@Param("roundId") Integer roundId);

    /**
     * Tìm Project theo Council Block ID
     */
    @Query("SELECT DISTINCT rp FROM RoundProject rp " +
            "JOIN rp.roundBlocks rb " +
            "WHERE rb.councilBlock.blockId = :councilId")
    List<RoundProject> findByCouncilId(@Param("councilId") Integer councilId);

    /**
     * QUAN TRỌNG: Tìm các project CHƯA xếp lịch và ĐỦ ĐIỀU KIỆN (PENDING)
     * - roundBlocks IS EMPTY: Chưa vào hội đồng nào (Danh sách Kíp trống)
     * - project.status = PENDING: Đang chờ bảo vệ (không phải Deleted hay Completed)
     * - resultStatus = IN_PROGRESS: Chưa có điểm
     */
    // MỚI: Đổi `rp.roundBlock IS NULL` thành `rp.roundBlocks IS EMPTY`
    @Query("SELECT rp FROM RoundProject rp " +
            "WHERE rp.defenseRound.roundId = :roundId " +
            "AND rp.roundBlocks IS EMPTY " +
            "AND rp.project.status = :projectStatus " +
            "AND rp.resultStatus = :resultStatus")
    List<RoundProject> findUnassignedPendingProjects(
            @Param("roundId") Integer roundId,
            @Param("projectStatus") ProjectStatus projectStatus,
            @Param("resultStatus") RoundProjectStatus resultStatus
    );

    boolean existsByDefenseRound_RoundIdAndProject_ProjectId(Integer roundId, Integer projectId);

    List<RoundProject> findByDefenseRound_RoundIdAndProject_ProjectIdIn(Integer roundId, List<Integer> projectIds);
}