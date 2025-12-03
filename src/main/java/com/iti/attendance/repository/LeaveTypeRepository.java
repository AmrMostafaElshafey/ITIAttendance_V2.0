package com.iti.attendance.repository;

import com.iti.attendance.model.LeaveType;
import com.iti.attendance.model.RequestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {
    List<LeaveType> findByDeletedFalse();

    List<LeaveType> findByDeletedFalseAndRequestType(RequestType requestType);

    @Query("SELECT lt FROM LeaveType lt WHERE lt.deleted = false AND (lt.requestType = :requestType OR (:includeNull = true AND lt.requestType IS NULL))")
    List<LeaveType> findByDeletedFalseAndRequestTypeOrNull(@Param("requestType") RequestType requestType, @Param("includeNull") boolean includeNull);
}
