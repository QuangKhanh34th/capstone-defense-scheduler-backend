package com.capstone.scheduler.repository;

import com.capstone.scheduler.entity.ProjectSupervisor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectSupervisorRepository extends JpaRepository<ProjectSupervisor, Integer> {
}