package com.iti.attendance.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
public class AccessPolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String allowedPages;
    private boolean deleted = false;

    @Transient
    private List<String> allowedPageList = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getAllowedPages() {
        return allowedPages;
    }

    public void setAllowedPages(String allowedPages) {
        this.allowedPages = allowedPages;
    }

    public List<String> getAllowedPageList() {
        if ((allowedPageList == null || allowedPageList.isEmpty()) && allowedPages != null && !allowedPages.isBlank()) {
            allowedPageList = new ArrayList<>(Arrays.stream(allowedPages.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList());
        }
        return allowedPageList;
    }

    public void setAllowedPageList(List<String> allowedPageList) {
        this.allowedPageList = allowedPageList;
        if (allowedPageList != null && !allowedPageList.isEmpty()) {
            this.allowedPages = String.join(",", allowedPageList);
        } else {
            this.allowedPages = null;
        }
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
