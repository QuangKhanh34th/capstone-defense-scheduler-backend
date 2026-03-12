package com.capstone.scheduler.repository;

import com.capstone.scheduler.entity.DeviceToken;
import com.capstone.scheduler.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    Optional<DeviceToken> findByToken(String token);
    List<DeviceToken> findAllByUser(User user);
    
    @org.springframework.data.jpa.repository.Query("SELECT dt FROM DeviceToken dt " +
           "JOIN dt.user u " +
           "WHERE u.role = :role")
    List<DeviceToken> findAllByUserRole(@org.springframework.data.repository.query.Param("role") com.capstone.scheduler.enums.UserRole role);
    
    void deleteByToken(String token);
}
