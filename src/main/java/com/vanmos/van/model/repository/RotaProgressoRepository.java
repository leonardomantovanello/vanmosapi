package com.vanmos.van.model.repository;

import com.vanmos.van.model.entity.RotaProgresso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RotaProgressoRepository extends JpaRepository<RotaProgresso, Long> {
}
