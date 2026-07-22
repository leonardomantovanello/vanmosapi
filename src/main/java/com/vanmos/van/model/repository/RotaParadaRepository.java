package com.vanmos.van.model.repository;

import com.vanmos.van.model.entity.RotaParada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RotaParadaRepository extends JpaRepository<RotaParada, Long> {
    List<RotaParada> findByMotoristaIdOrderByOrdemAsc(Long motoristaId);
    Optional<RotaParada> findByIdAndMotoristaId(Long id, Long motoristaId);
    boolean existsByMotoristaIdAndAlunoId(Long motoristaId, Long alunoId);

    @Query("SELECT COALESCE(MAX(r.ordem), 0) FROM RotaParada r WHERE r.motoristaId = :motoristaId")
    int maiorOrdem(@Param("motoristaId") Long motoristaId);
}
