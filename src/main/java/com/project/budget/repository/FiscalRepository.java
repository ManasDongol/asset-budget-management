package com.project.budget.repository;

import com.project.budget.entity.FiscalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface FiscalRepository extends JpaRepository<FiscalEntity, Long> {

    // Find the fiscal year that contains a specific date
    @Query("SELECT f FROM FiscalEntity f WHERE :currentDate BETWEEN f.startDate AND f.endDate")
    FiscalEntity findByDate(@Param("currentDate") LocalDate currentDate);

    // Optional: get latest fiscal year
    FiscalEntity findTopByOrderByFiscalIdDesc();
}
