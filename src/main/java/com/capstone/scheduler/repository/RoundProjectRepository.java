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
        @Query("SELECT rp FROM RoundProject rp " +
                "JOIN FETCH rp.project " +
                "WHERE rp.defenseRound.roundId = :roundId")
        List<RoundProject> findByRoundId(@Param("roundId") Integer roundId);

        /**
         * Tìm Project theo Council Block ID
         */
    @Query("SELECT rb.roundProject FROM RoundBlock rb WHERE rb.councilBlock.blockId = :councilId")
        List<RoundProject> findByCouncilId(@Param("councilId") Integer councilId);

        /**
         * QUAN TRỌNG: Tìm các project CHƯA xếp lịch và ĐỦ ĐIỀU KIỆN (PENDING)
     * - Không tồn tại trong bảng RoundBlock (chưa được gán)
         * - project.status = PENDING: Đang chờ bảo vệ (không phải Deleted hay Completed)
         * - resultStatus = IN_PROGRESS: Chưa có điểm
         */
        @Query("SELECT rp FROM RoundProject rp " +
                "WHERE rp.defenseRound.roundId = :roundId " +
            "AND NOT EXISTS (SELECT rb FROM RoundBlock rb WHERE rb.roundProject = rp) " +
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
