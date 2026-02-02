package com.capstone.scheduler.repository;

import com.capstone.scheduler.entity.LecturerCompatibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LecturerCompatibilityRepository extends JpaRepository<LecturerCompatibility, Integer> {
    // Tìm độ tương thích giữa 2 ông GV (Kiểm tra cả 2 chiều A->B và B->A)
    @Query("SELECT lc FROM LecturerCompatibility lc " +
            "WHERE (lc.lecturer1.lecturerId = :id1 AND lc.lecturer2.lecturerId = :id2) " +
            "OR (lc.lecturer1.lecturerId = :id2 AND lc.lecturer2.lecturerId = :id1)")
    Optional<LecturerCompatibility> findCompatibility(Integer id1, Integer id2);
}