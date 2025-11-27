package com.iti.attendance.repository;

import com.iti.attendance.model.MissionRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRequestRepository extends JpaRepository<MissionRequest, Long> {
}
