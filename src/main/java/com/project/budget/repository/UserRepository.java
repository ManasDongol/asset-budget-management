package com.project.budget.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.budget.entity.userEntity;


public interface UserRepository extends JpaRepository<userEntity, Long> {
   
	 userEntity findByUsername(String username);

	  List<userEntity> findTop10ByOrderByIdDesc();
	  
	  List<userEntity> findByUsernameContainingIgnoreCase(String username);

}

