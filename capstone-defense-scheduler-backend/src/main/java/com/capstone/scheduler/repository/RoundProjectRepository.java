    package com.capstone.scheduler.repository;

    import com.capstone.scheduler.entity.RoundProject;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.data.jpa.repository.Query;
    import org.springframework.data.repository.query.Param;
    import org.springframework.stereotype.Repository;

    import java.util.List;

    @Repository
    public interface RoundProjectRepository extends JpaRepository<RoundProject, Integer> {

        /**
         * Find all round projects with eagerly loaded Project and Council (with CouncilBlock)
         */
        @Query("SELECT rp FROM RoundProject rp " +
               "JOIN FETCH rp.project " +
                "LEFT JOIN FETCH rp.roundBlock rb " +
                "LEFT JOIN FETCH rb.councilBlock " +
               "WHERE rp.defenseRound.roundId = :roundId")
        List<RoundProject> findByRoundId(@Param("roundId") Integer roundId);

        @Query("SELECT rp FROM RoundProject rp WHERE rp.roundBlock.roundBlockId = :councilId")
        List<RoundProject> findByCouncilId(@Param("councilId") Integer councilId);

        // Tìm các project chưa được xếp lịch (chưa có nhóm)
        List<RoundProject> findByDefenseRound_RoundIdAndRoundBlockIsNull(Integer roundId);

        boolean existsByDefenseRound_RoundIdAndProject_ProjectId(Integer roundId, Integer projectId);

        // Tìm list RoundProject dựa trên list Project ID và Round ID
        List<RoundProject> findByDefenseRound_RoundIdAndProject_ProjectIdIn(Integer roundId, List<Integer> projectIds);

    }
