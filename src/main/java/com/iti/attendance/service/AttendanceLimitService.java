package com.iti.attendance.service;

import com.iti.attendance.model.AttendanceLimit;
import com.iti.attendance.repository.AttendanceLimitRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AttendanceLimitService {
    private final AttendanceLimitRepository attendanceLimitRepository;

    public AttendanceLimitService(AttendanceLimitRepository attendanceLimitRepository) {
        this.attendanceLimitRepository = attendanceLimitRepository;
    }

    public Optional<AttendanceLimit> getActiveLimit() {
        return attendanceLimitRepository.findTopByActiveTrueOrderByUpdatedAtDesc();
    }

    public AttendanceLimit save(AttendanceLimit limit) {
        if (limit.isActive()) {
            attendanceLimitRepository.findAll().forEach(existing -> {
                existing.setActive(false);
                attendanceLimitRepository.save(existing);
            });
        }
        limit.setUpdatedAt(java.time.LocalDateTime.now());
        return attendanceLimitRepository.save(limit);
    }
}
