package com.iti.attendance.repository;

import com.iti.attendance.model.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findByDateAndDeletedFalse(LocalDate date);
    List<AttendanceRecord> findByDeletedFalse();
    List<AttendanceRecord> findByEmployeeIdAndDeletedFalse(Long employeeId);
}
