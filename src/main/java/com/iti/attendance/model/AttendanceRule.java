package com.iti.attendance.model;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
public class AttendanceRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    private Organization organization;

    @ManyToOne
    private Branch branch;

    @ManyToOne
    private Department department;

    private LocalTime expectedCheckIn;
    private LocalTime expectedCheckOut;
    private int graceMinutes;
    private boolean overtimeAllowed;
    private boolean deleted = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public LocalTime getExpectedCheckIn() {
        return expectedCheckIn;
    }

    public void setExpectedCheckIn(LocalTime expectedCheckIn) {
        this.expectedCheckIn = expectedCheckIn;
    }

    public LocalTime getExpectedCheckOut() {
        return expectedCheckOut;
    }

    public void setExpectedCheckOut(LocalTime expectedCheckOut) {
        this.expectedCheckOut = expectedCheckOut;
    }

    public int getGraceMinutes() {
        return graceMinutes;
    }

    public void setGraceMinutes(int graceMinutes) {
        this.graceMinutes = graceMinutes;
    }

    public boolean isOvertimeAllowed() {
        return overtimeAllowed;
    }

    public void setOvertimeAllowed(boolean overtimeAllowed) {
        this.overtimeAllowed = overtimeAllowed;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
