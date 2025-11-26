package com.iti.attendance.repository;

import com.iti.attendance.model.AttendanceLimit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttendanceLimitRepository extends JpaRepository<AttendanceLimit, Long> {
    Optional<AttendanceLimit> findTopByActiveTrueOrderByUpdatedAtDesc();
}
