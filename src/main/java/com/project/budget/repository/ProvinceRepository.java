package com.project.budget.repository;

import com.project.budget.entity.ProvinceEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProvinceRepository extends JpaRepository<ProvinceEntity, String> {

    ProvinceEntity findByProvinceName(String provinceName);

    ProvinceEntity findByProvinceCode(String provinceCode);
    
    List<ProvinceEntity> findTop5ByOrderByCreatedAtDesc();

}
