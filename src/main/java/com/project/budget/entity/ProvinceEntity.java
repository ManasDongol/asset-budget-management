package com.project.budget.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "province")
public class ProvinceEntity {

    @Id
    @Column(name = "province_code", length = 10, nullable = false, unique = true)
    private String provinceCode;

    @Column(name = "province_name", length = 100, nullable = false)
    private String provinceName;
    
    @Column(name = "province_name_Nepali", length = 100, nullable = false)
    private String provinceNameNepali;

    @Column(name = "province_address", length = 255)
    private String provinceAddress;

    @Column(name = "headoffice_code", length = 10)
    private String headOfficeCode;

    @Column(name = "created_at", updatable = false	)
    private LocalDateTime createdAt;

    public ProvinceEntity() {}

    public ProvinceEntity(String provinceCode, String provinceName, String provinceAddress, String headOfficeCode) {
        this.provinceCode = provinceCode;
        this.provinceName = provinceName;
        this.provinceAddress = provinceAddress;
        this.headOfficeCode = headOfficeCode;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // Getters & Setters
    public String getProvinceCode() { return provinceCode; }
    public void setProvinceCode(String provinceCode) { this.provinceCode = provinceCode; }

    public String getProvinceName() { return provinceName; }
    public void setProvinceName(String provinceName) { this.provinceName = provinceName; }
    
    public String getProvinceNameNepali() { return provinceNameNepali; }
    public void setProvinceNameNepali(String provinceNameNepali) { this.provinceNameNepali = provinceNameNepali; }

    public String getProvinceAddress() { return provinceAddress; }
    public void setProvinceAddress(String provinceAddress) { this.provinceAddress = provinceAddress; }

    public String getHeadOfficeCode() { return headOfficeCode; }
    public void setHeadOfficeCode(String headOfficeCode) { this.headOfficeCode = headOfficeCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
