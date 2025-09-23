package com.project.budget.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class userEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 50)
    private String username;

    @Column(length = 255)
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_code", referencedColumnName = "staff_code")
    private StaffEntity staff;

    @Column(length = 50)
    private String inputer;

    @Column(length = 50)
    private String authoriser;

    @Column(name = "created_at", columnDefinition = "DATETIME")
    private LocalDateTime createdAt;

    @Column(name = "userStatus", length = 20)
    private String userStatus;

    public userEntity() {}

    public userEntity(String username, String password, StaffEntity staff, String inputer, String authoriser, String userStatus) {
        this.username = username;
        this.password = password;
        this.staff = staff;
        this.inputer = inputer;
        this.authoriser = authoriser;
        this.userStatus = userStatus;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public StaffEntity getStaff() { return staff; }
    public void setStaff(StaffEntity staff) { this.staff = staff; }

    public String getInputer() { return inputer; }
    public void setInputer(String inputer) { this.inputer = inputer; }

    public String getAuthoriser() { return authoriser; }
    public void setAuthoriser(String authoriser) { this.authoriser = authoriser; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getUserStatus() { return userStatus; }
    public void setUserStatus(String userStatus) { this.userStatus = userStatus; }
}
