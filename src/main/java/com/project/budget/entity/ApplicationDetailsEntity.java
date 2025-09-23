package com.project.budget.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Application_Details")
public class ApplicationDetailsEntity {

    @Id
    @Column(name = "application_number")
    private String applicationNumber; // primary key

    @Column(name = "fiscal_year")
    private String fiscalYear;

    private String toWhom;
    private String fromWhom;
    private String date;
    private String subject;
    private String cc;

    private String staffFullName;
    private String staffPost;
    private String staffCode;

    @OneToMany(mappedBy = "applicationDetailsEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssetHistoryEntity> assetHistories = new ArrayList<>();

    // Getters & Setters
    public String getApplicationNumber() { return applicationNumber; }
    public void setApplicationNumber(String applicationNumber) { this.applicationNumber = applicationNumber; }

    public String getFiscalYear() { return fiscalYear; }
    public void setFiscalYear(String fiscalYear) { this.fiscalYear = fiscalYear; }

    public String getToWhom() { return toWhom; }
    public void setToWhom(String toWhom) { this.toWhom = toWhom; }

    public String getFromWhom() { return fromWhom; }
    public void setFromWhom(String fromWhom) { this.fromWhom = fromWhom; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getCc() { return cc; }
    public void setCc(String cc) { this.cc = cc; }

    public String getStaffFullName() { return staffFullName; }
    public void setStaffFullName(String staffFullName) { this.staffFullName = staffFullName; }

    public String getStaffPost() { return staffPost; }
    public void setStaffPost(String staffPost) { this.staffPost = staffPost; }

    public String getStaffCode() { return staffCode; }
    public void setStaffCode(String staffCode) { this.staffCode = staffCode; }

    public List<AssetHistoryEntity> getAssetHistories() { return assetHistories; }
    public void setAssetHistories(List<AssetHistoryEntity> assetHistories) { this.assetHistories = assetHistories; }
}
