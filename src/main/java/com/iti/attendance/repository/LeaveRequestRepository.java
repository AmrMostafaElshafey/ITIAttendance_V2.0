package com.iti.attendance.repository;

import com.iti.attendance.model.LeaveRequest;
import com.iti.attendance.model.RequestType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByDeletedFalse();

    List<LeaveRequest> findByEmployeeIdAndDeletedFalse(Long employeeId);

    List<LeaveRequest> findByEmployeeIdAndDeletedFalseAndType(Long employeeId, RequestType type);

    List<LeaveRequest> findByDeletedFalseAndType(RequestType type);
}
