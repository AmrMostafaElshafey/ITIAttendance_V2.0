package com.iti.attendance.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class AttendanceLimit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int limitPercent = 80;
    private String description;
    private boolean active = true;
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getLimitPercent() {
        return limitPercent;
    }

    public void setLimitPercent(int limitPercent) {
        this.limitPercent = limitPercent;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
