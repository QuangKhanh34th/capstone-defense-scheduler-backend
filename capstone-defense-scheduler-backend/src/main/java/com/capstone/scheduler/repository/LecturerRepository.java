package com.capstone.scheduler.repository;

import com.capstone.scheduler.entity.Lecturer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LecturerRepository extends JpaRepository<Lecturer, Integer> {

    Optional<Lecturer> findByFullNameIgnoreCase(String fullName);
}