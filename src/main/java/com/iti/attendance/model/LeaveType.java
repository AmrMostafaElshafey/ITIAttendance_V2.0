package com.iti.attendance.model;

import jakarta.persistence.*;

@Entity
public class LeaveType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private int maxDaysPerRequest;
    private boolean requiresManagerApproval;
    private boolean requiresHrApproval;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMaxDaysPerRequest() {
        return maxDaysPerRequest;
    }

    public void setMaxDaysPerRequest(int maxDaysPerRequest) {
        this.maxDaysPerRequest = maxDaysPerRequest;
    }

    public boolean isRequiresManagerApproval() {
        return requiresManagerApproval;
    }

    public void setRequiresManagerApproval(boolean requiresManagerApproval) {
        this.requiresManagerApproval = requiresManagerApproval;
    }

    public boolean isRequiresHrApproval() {
        return requiresHrApproval;
    }

    public void setRequiresHrApproval(boolean requiresHrApproval) {
        this.requiresHrApproval = requiresHrApproval;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
