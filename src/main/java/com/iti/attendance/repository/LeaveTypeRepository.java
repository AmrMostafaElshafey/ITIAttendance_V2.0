package com.iti.attendance.repository;

import com.iti.attendance.model.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {
    List<LeaveType> findByDeletedFalse();
}
