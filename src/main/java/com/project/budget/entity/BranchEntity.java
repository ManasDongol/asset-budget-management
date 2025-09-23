package com.project.budget.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "branch")
public class BranchEntity {

    @Id
    @Column(name = "branch_code", length = 10)
    private String branchCode;

    @Column(name = "branch_name", length = 100)
    private String branchName;
    
    @Column(name = "branch_name_Nepali", length = 100)
    private String branchNameNepali;

    @Column(name = "branch_address", length = 255)
    private String branchAddress;

    // Foreign key to ProvinceEntity
    @ManyToOne
    @JoinColumn(name = "province_code", referencedColumnName = "province_code", nullable = false)
    private ProvinceEntity provinceCode;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public BranchEntity() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters & Setters
    public String getBranchCode() { return branchCode; }
    public void setBranchCode(String branchCode) { this.branchCode = branchCode; }

    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }
    
    public String getBranchNameNepali() { return branchNameNepali; }
    public void setBranchNameNepali(String branchNameNepali) { this.branchNameNepali = branchNameNepali; }

    public String getBranchAddress() { return branchAddress; }
    public void setBranchAddress(String branchAddress) { this.branchAddress = branchAddress; }

    // Correct getters/setters for ProvinceEntity
    public ProvinceEntity getProvinceCode() { return provinceCode; }
    public void setProvinceCode(ProvinceEntity provinceCode) { this.provinceCode = provinceCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

