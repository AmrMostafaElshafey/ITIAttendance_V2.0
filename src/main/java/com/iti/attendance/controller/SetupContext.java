package com.iti.attendance.controller;

public class SetupContext {
    private Long organizationId;

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public boolean hasOrganization() {
        return organizationId != null;
    }
}
