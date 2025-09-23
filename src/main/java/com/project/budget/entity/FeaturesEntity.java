package com.project.budget.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "features")
public class FeaturesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Feature", nullable = false, unique = true)
    private String feature; // e.g., "Add User"

    @Column(name = "Status", nullable = false)
    private int status; // 1 = Active, 0 = Inactive

    // Constructors
    public FeaturesEntity() {}

    public FeaturesEntity(String feature, int status) {
        this.feature = feature;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "FeaturesEntity{" +
                "id=" + id +
                ", feature='" + feature + '\'' +
                ", status=" + status +
                '}';
    }
}
