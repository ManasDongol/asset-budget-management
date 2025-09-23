package com.project.budget.repository;

import com.project.budget.entity.StaffEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StaffRepository extends JpaRepository<StaffEntity, String> {

    List<StaffEntity> findByStaffCodeContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
        String staffCode, String firstName, String lastName);

    // Fetch 5 most recent staff ordered by createdAt
    List<StaffEntity> findTop5ByOrderByCreatedAtDesc();
}

