package com.project.budget.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "fiscal")
public class FiscalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fiscalId;

    @Column(nullable = false, unique = true)
    private String fiscalYear; 

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    // Getters & Setters
    public Long getFiscalId() { return fiscalId; }
    public void setFiscalId(Long fiscalId) { this.fiscalId = fiscalId; }

    public String getFiscalYear() { return fiscalYear; }
    public void setFiscalYear(String fiscalYear) { this.fiscalYear = fiscalYear; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
