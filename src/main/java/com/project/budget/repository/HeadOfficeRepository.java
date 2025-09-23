package com.project.budget.repository;

import com.project.budget.entity.HeadOfficeEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HeadOfficeRepository extends JpaRepository<HeadOfficeEntity, String> {
    HeadOfficeEntity findByHeadofficeCode(String headofficeCode);
    
    List<HeadOfficeEntity> findTop5ByOrderByCreatedAtDesc();

}
