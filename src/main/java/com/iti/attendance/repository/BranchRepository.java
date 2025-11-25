package com.iti.attendance.repository;

import com.iti.attendance.model.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BranchRepository extends JpaRepository<Branch, Long> {
    List<Branch> findByDeletedFalse();
}
