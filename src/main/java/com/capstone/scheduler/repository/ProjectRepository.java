package com.capstone.scheduler.repository;

import com.capstone.scheduler.entity.Project;
import com.capstone.scheduler.enums.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer>, JpaSpecificationExecutor<Project> {
    // Tìm đề tài theo học kỳ
    List<Project> findBySemester_SemesterId(Integer semesterId);
    List<Project> findBySemester_SemesterIdAndStatus(Integer semesterId, ProjectStatus status);
}