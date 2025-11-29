package com.iti.attendance.repository;

import com.iti.attendance.model.PermissionRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRequestRepository extends JpaRepository<PermissionRequest, Long> {
}
