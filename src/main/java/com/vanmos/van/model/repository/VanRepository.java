package com.vanmos.van.model.repository;

import com.vanmos.van.model.entity.Van;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VanRepository extends JpaRepository<Van, Long> {
}