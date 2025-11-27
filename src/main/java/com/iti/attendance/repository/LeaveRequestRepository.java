package com.iti.attendance.repository;

import com.iti.attendance.model.LeaveRequest;
import com.iti.attendance.model.RequestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByDeletedFalse();

    List<LeaveRequest> findByEmployeeIdAndDeletedFalse(Long employeeId);

    List<LeaveRequest> findByEmployeeIdAndDeletedFalseAndType(Long employeeId, RequestType type);

    List<LeaveRequest> findByDeletedFalseAndType(RequestType type);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId AND lr.deleted = false AND (lr.type = :type OR (:includeNullType = true AND lr.type IS NULL))")
    List<LeaveRequest> findByEmployeeIdAndDeletedFalseAndTypeOrNull(@Param("employeeId") Long employeeId, @Param("type") RequestType type, @Param("includeNullType") boolean includeNullType);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.deleted = false AND (lr.type = :type OR (:includeNullType = true AND lr.type IS NULL))")
    List<LeaveRequest> findByDeletedFalseAndTypeOrNull(@Param("type") RequestType type, @Param("includeNullType") boolean includeNullType);
}
