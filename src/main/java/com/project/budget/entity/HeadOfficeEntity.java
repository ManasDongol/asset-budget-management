package com.project.budget.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "headoffice")
public class HeadOfficeEntity {

    @Id
    @Column(name = "headoffice_code", length = 10)
    private String headofficeCode;

    @Column(name = "headoffice_name", length = 100)
    private String headofficeName;

    @Column(name = "headoffice_address", length = 255)
    private String headofficeAddress;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public HeadOfficeEntity() {}

    public HeadOfficeEntity(String headofficeCode, String headofficeName, String headofficeAddress) {
        this.headofficeCode = headofficeCode;
        this.headofficeName = headofficeName;
        this.headofficeAddress = headofficeAddress;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters & Setters
    public String getHeadofficeCode() { return headofficeCode; }
    public void setHeadofficeCode(String headofficeCode) { this.headofficeCode = headofficeCode; }

    public String getHeadofficeName() { return headofficeName; }
    public void setHeadofficeName(String headofficeName) { this.headofficeName = headofficeName; }

    public String getHeadofficeAddress() { return headofficeAddress; }
    public void setHeadofficeAddress(String headofficeAddress) { this.headofficeAddress = headofficeAddress; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
