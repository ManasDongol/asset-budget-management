package com.project.budget.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "staff")
public class StaffEntity {

    @Id
    @Column(name = "staff_code", length = 10)
    private String staffCode;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @JoinColumn(name = "office_code")
    private String officeCode;
    
    @ManyToOne
    @JoinColumn(name = "branch_code", referencedColumnName = "branch_code")
    private BranchEntity branch;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public StaffEntity() {}

    public StaffEntity(String staffCode, String firstName, String lastName, String officeCode) {
        this.staffCode = staffCode;
        this.firstName = firstName;
        this.lastName = lastName;
        this.officeCode = officeCode;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters & Setters
    public String getStaffCode() { return staffCode; }
    public void setStaffCode(String staffCode) { this.staffCode = staffCode; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getOfficeCode() { return officeCode; }
    public void setOfficeCode(String officeCode) { this.officeCode = officeCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public BranchEntity getBranch() { return branch; }
    public void setBranch(BranchEntity branch) { this.branch = branch; }
}
