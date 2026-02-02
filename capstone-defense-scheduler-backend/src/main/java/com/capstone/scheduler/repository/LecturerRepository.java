package com.capstone.scheduler.repository;

import com.capstone.scheduler.entity.Lecturer;
import com.capstone.scheduler.enums.CommonStatus; // Import Enum này
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LecturerRepository extends JpaRepository<Lecturer, Integer>, JpaSpecificationExecutor<Lecturer> {

    /**
     * SỬA: Thay findByIsActiveTrue() bằng findByStatus()
     * Spring Data JPA sẽ tự hiểu tìm theo cột 'status'
     */
    List<Lecturer> findByStatus(CommonStatus status);

    /**
     * SỬA: Thay IsActiveTrue bằng Status
     */
    List<Lecturer> findByDepartment_DepartmentIdAndStatus(Integer departmentId, CommonStatus status);

    /**
     * Find lecturer by code (Giữ nguyên logic)
     */
    @Query("SELECT l FROM Lecturer l WHERE l.lecturerCode = :code")
    Optional<Lecturer> findByLecturerCode(@Param("code") String lecturerCode);

    /**
     * SỬA: Cập nhật câu JPQL dùng status thay vì isActive
     * Lưu ý: 'ACTIVE' ở đây khớp với EnumType.STRING trong Entity
     */
    @Query("SELECT l FROM Lecturer l " +
            "JOIN FETCH l.user " +
            "JOIN FETCH l.department " +
            "WHERE l.status = 'ACTIVE'")
    List<Lecturer> findAllActiveWithDetails();

    // Các hàm dưới giữ nguyên vì không dính đến status
    boolean existsByLecturerCode(String lecturerCode);
    boolean existsByEmail(String email);

    Optional<Lecturer> findByUser_Username(String username);

    Optional<Lecturer> findByUser_UserId(Integer userId);
}