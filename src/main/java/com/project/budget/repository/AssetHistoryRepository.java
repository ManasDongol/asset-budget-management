package com.project.budget.repository;

import com.project.budget.entity.AssetHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetHistoryRepository extends JpaRepository<AssetHistoryEntity, Long> {

	@Query("SELECT a FROM AssetHistoryEntity a JOIN FETCH a.branch WHERE a.branch.branchCode = :branchCode")
	List<AssetHistoryEntity> findByBranch_BranchCode(@Param("branchCode") String branchCode);
}
