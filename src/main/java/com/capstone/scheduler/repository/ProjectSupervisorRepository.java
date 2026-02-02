package com.capstone.scheduler.repository;

import com.capstone.scheduler.entity.ProjectSupervisor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectSupervisorRepository extends JpaRepository<ProjectSupervisor, Integer> {

    @Query("SELECT ps FROM ProjectSupervisor ps WHERE ps.project.projectId = :projectId")
    List<ProjectSupervisor> findByProjectId(@Param("projectId") Integer projectId);

    @Query("SELECT ps FROM ProjectSupervisor ps WHERE ps.lecturer.lecturerId = :lecturerId")
    List<ProjectSupervisor> findByLecturerId(@Param("lecturerId") Integer lecturerId);

    /**
     * Bulk fetch all supervisors for a list of project IDs (avoids N+1 queries)
     */
    @Query("SELECT ps FROM ProjectSupervisor ps " +
           "JOIN FETCH ps.lecturer " +
           "JOIN FETCH ps.project " +
           "WHERE ps.project.projectId IN :projectIds")
    List<ProjectSupervisor> findByProjectIdIn(@Param("projectIds") List<Integer> projectIds);

    // Lấy danh sách GVHD của 1 project
    List<ProjectSupervisor> findByProject_ProjectId(Integer projectId);

    // Kiểm tra xem GV này đã hướng dẫn project này chưa
    boolean existsByProject_ProjectIdAndLecturer_LecturerId(Integer projectId, Integer lecturerId);

    // Kiểm tra xem Project đã có GVHD Chính
    boolean existsByProject_ProjectIdAndRoleType(Integer projectId, String roleType);
}
