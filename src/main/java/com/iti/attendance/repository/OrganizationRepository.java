package com.iti.attendance.repository;

import com.iti.attendance.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    List<Organization> findByDeletedFalse();
}
