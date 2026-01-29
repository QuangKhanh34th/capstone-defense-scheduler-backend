package com.capstone.scheduler.repository;

import com.capstone.scheduler.entity.Lecturer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LecturerRepository extends JpaRepository<Lecturer, Integer>, JpaSpecificationExecutor<Lecturer> {

    /**
     * Find all active lecturers
     */
    List<Lecturer> findByIsActiveTrue();

    /**
     * Find lecturers by department
     */
    List<Lecturer> findByDepartment_DepartmentIdAndIsActiveTrue(Integer departmentId);

    /**
     * Find lecturer by code
     */
    @Query("SELECT l FROM Lecturer l WHERE l.lecturerCode = :code")
    Lecturer findByLecturerCode(@Param("code") String code);

    @Query("SELECT l FROM Lecturer l JOIN FETCH l.user JOIN FETCH l.department WHERE l.isActive = true")
    List<Lecturer> findAllActiveWithDetails();
}
