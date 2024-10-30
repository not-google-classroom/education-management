package com.org.education_management.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class OrgDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long orgID;
    private Long userID;

    private LocalDateTime createdAt = LocalDateTime.now();

    public OrgDetails(Long orgID, Long userID, LocalDateTime createdAt) {
        this.orgID = orgID;
        this.userID = userID;
        this.createdAt = createdAt;
    }

    public Long getOrgID() {
        return orgID;
    }

    public void setOrgID(Long orgID) {
        this.orgID = orgID;
    }

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
