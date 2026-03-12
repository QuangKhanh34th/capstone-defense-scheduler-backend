package com.capstone.scheduler.repository;

import com.capstone.scheduler.entity.Semester;
import com.capstone.scheduler.enums.SemesterStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, Integer>, JpaSpecificationExecutor<Semester> {

    Optional<Semester> findByStatus(String status);
    boolean existsByName(String name);
    List<Semester> findByStatus(SemesterStatus status);
}