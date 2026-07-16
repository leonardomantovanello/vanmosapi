package com.vanmos.van.model.repository;

import com.vanmos.van.model.entity.Falta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FaltaRepository extends JpaRepository<Falta, Long> {
    List<Falta> findByAlunoIdOrderByDataAsc(Long alunoId);
    Optional<Falta> findByAlunoIdAndData(Long alunoId, LocalDate data);
    void deleteByAlunoIdAndData(Long alunoId, LocalDate data);
}
