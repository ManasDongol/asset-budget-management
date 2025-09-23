package com.project.budget.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.budget.entity.FeaturesEntity;

import java.util.Optional;

public interface FeaturesRepository extends JpaRepository<FeaturesEntity, Long> {
    Optional<FeaturesEntity> findByFeature(String feature);
}
