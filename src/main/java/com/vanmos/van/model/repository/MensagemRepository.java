package com.vanmos.van.model.repository;

import com.vanmos.van.model.entity.Mensagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensagemRepository extends JpaRepository<Mensagem, Long> {
    List<Mensagem> findByAlunoIdOrderByCriadoEmAsc(Long alunoId);
}
