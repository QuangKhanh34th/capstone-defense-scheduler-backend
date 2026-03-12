package com.capstone.scheduler.repository;

import com.capstone.scheduler.entity.RoundProject;
import com.capstone.scheduler.enums.ProjectStatus;
import com.capstone.scheduler.enums.RoundProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoundProjectRepository extends JpaRepository<RoundProject, Integer>, JpaSpecificationExecutor<RoundProject> {

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
     * QUAN TRỌNG: Tìm các project CHƯA xếp lịch và ĐỦ ĐIỀU KIỆN
     * - roundBlocks IS EMPTY: Chưa vào hội đồng nào (Danh sách Kíp trống)
     * - project.status IN :projectStatuses: Nằm trong danh sách trạng thái cho phép (VD: PENDING)
     * - resultStatus = IN_PROGRESS: Chưa có điểm
     */
    @Query("SELECT rp FROM RoundProject rp " +
            "WHERE rp.defenseRound.roundId = :roundId " +
            "AND rp.roundBlocks IS EMPTY " +
            "AND rp.project.status IN :projectStatuses " +
            "AND rp.resultStatus = :resultStatus")
    List<RoundProject> findUnassignedPendingProjects(
            @Param("roundId") Integer roundId,
            @Param("projectStatuses") List<ProjectStatus> projectStatuses,
            @Param("resultStatus") RoundProjectStatus resultStatus
    );

    boolean existsByDefenseRound_RoundIdAndProject_ProjectId(Integer roundId, Integer projectId);

    List<RoundProject> findByDefenseRound_RoundIdAndProject_ProjectIdIn(Integer roundId, List<Integer> projectIds);

    void deleteByDefenseRound_RoundId(Integer roundId);
    void deleteByProject_ProjectId(Integer projectId);
    List<RoundProject> findByDefenseRound_RoundIdAndResultStatusAndProject_Status(
            Integer roundId,
            RoundProjectStatus resultStatus,
            ProjectStatus projectStatus);
    boolean existsByDefenseRound_RoundIdAndResultStatus(Integer roundId, RoundProjectStatus resultStatus);
}