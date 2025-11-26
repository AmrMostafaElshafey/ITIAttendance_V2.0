package com.iti.attendance.repository;

import com.iti.attendance.model.Employee;
import com.iti.attendance.model.EmployeeStatus;
import com.iti.attendance.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByDeletedFalse();
    List<Employee> findByStatus(EmployeeStatus status);
    Optional<Employee> findByEmailAndDeletedFalse(String email);
    List<Employee> findByRoleAndDeletedFalse(Role role);
    List<Employee> findByRoleInAndDeletedFalse(List<Role> roles);
    List<Employee> findByManagerAndDeletedFalse(Employee manager);
}
