package com.iti.attendance.repository;

import com.iti.attendance.model.AttendanceRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttendanceRuleRepository extends JpaRepository<AttendanceRule, Long> {
    List<AttendanceRule> findByDeletedFalse();
}
