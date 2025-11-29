package com.iti.attendance.repository;

import com.iti.attendance.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByDeletedFalse();
    List<Department> findByBranchIdAndDeletedFalse(Long branchId);
    List<Department> findByBranchIdInAndDeletedFalse(List<Long> branchIds);
}
