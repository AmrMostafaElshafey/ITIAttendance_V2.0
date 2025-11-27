package com.iti.attendance.repository;

import com.iti.attendance.model.JobTitle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobTitleRepository extends JpaRepository<JobTitle, Long> {
    List<JobTitle> findByDeletedFalse();
}
