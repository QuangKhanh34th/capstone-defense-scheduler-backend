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
}
