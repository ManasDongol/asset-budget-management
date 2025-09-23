package com.project.budget.repository;

import com.project.budget.entity.BranchEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchRepository extends JpaRepository<BranchEntity, String> {

    BranchEntity findByBranchName(String branchName);

    BranchEntity findByBranchCode(String branchCode);
    
    List<BranchEntity> findTop5ByOrderByCreatedAtDesc();
}
