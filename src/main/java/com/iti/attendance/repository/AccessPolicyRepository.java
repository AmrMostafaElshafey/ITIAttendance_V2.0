package com.iti.attendance.repository;

import com.iti.attendance.model.AccessPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccessPolicyRepository extends JpaRepository<AccessPolicy, Long> {
    List<AccessPolicy> findByDeletedFalse();
}
