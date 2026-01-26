package com.capstone.scheduler.repository;

import com.capstone.scheduler.entity.CouncilRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouncilRoleRepository extends JpaRepository<CouncilRole, Integer> {

    Optional<CouncilRole> findByRoleCode(String roleCode);
}
