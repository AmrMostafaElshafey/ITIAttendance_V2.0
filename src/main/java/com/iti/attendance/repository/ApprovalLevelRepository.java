package com.iti.attendance.repository;

import com.iti.attendance.model.ApprovalLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalLevelRepository extends JpaRepository<ApprovalLevel, Long> {
    List<ApprovalLevel> findByDeletedFalseOrderByLevelOrderAsc();
}
