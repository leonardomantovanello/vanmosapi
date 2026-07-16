package com.vanmos.van.model.service;

import com.vanmos.van.exception.DuplicateResourceException;
import com.vanmos.van.exception.ResourceNotFoundException;
import com.vanmos.van.model.entity.Aluno;
import com.vanmos.van.model.repository.AlunoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service de Aluno com tratamento de erro isolado (ponto 1 do relatório).
 *
 * PADRÃO APLICADO:
 *  - Exceções técnicas do banco (DataIntegrityViolationException, etc.) são
 *    capturadas aqui e relançadas como exceções de negócio (DuplicateResourceException,
 *    ResourceNotFoundException) sem detalhes internos.
 *  - O GlobalExceptionHandler converte essas exceções em respostas HTTP limpas.
 *  - Detalhes técnicos ficam apenas no log (SLF4J), nunca no response body.
 *
 * SQL INJECTION (ponto 2):
 *  Todos os métodos do JpaRepository usam Prepared Statements internamente.
 *  O Hibernate nunca concatena parâmetros diretamente na query SQL.
 *  Exemplo do SQL gerado: SELECT * FROM alunos WHERE id = ?  (não WHERE id = '42')
 */
@Service
@Transactional
public class AlunoService {

    private static final Logger log = LoggerFactory.getLogger(AlunoService.class);

    @Autowired
    private AlunoRepository alunoRepository;

    @Transactional(readOnly = true)
    public List<Aluno> findAll() {
        return alunoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Aluno> findById(Long id) {
        return alunoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Aluno> findByResponsavelId(Long responsavelId) {
        return alunoRepository.findByResponsavelId(responsavelId);
    }

    @Transactional(readOnly = true)
    public List<Aluno> findByMotoristaId(Long motoristaId) {
        return alunoRepository.findByMotoristaId(motoristaId);
    }

    public Aluno save(Aluno aluno) {
        try {
            return alunoRepository.save(aluno);
        } catch (DataIntegrityViolationException ex) {
            // Loga o detalhe técnico internamente
            log.error("Violação de integridade ao salvar Aluno: {}", ex.getMostSpecificCause().getMessage());
            // Relança exceção de negócio sem detalhes do banco
            throw new DuplicateResourceException("Já existe um aluno com esses dados.");
        }
    }

    public Aluno update(Long id, Aluno aluno) {
        Aluno existente = alunoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aluno", id));
        try {
            // Atualiza apenas campos editáveis — preserva 'ativo' e 'id' originais
            existente.setNome(aluno.getNome());
            existente.setTelefoneResponsavel(aluno.getTelefoneResponsavel());
            existente.setEnderecoEmbarque(aluno.getEnderecoEmbarque());
            existente.setEnderecoDesembarque(aluno.getEnderecoDesembarque());
            existente.setEscola(aluno.getEscola());
            existente.setTurno(aluno.getTurno());
            if (aluno.getResponsavelId() != null) {
                existente.setResponsavelId(aluno.getResponsavelId());
            }
            if (aluno.getMotoristaId() != null) {
                existente.setMotoristaId(aluno.getMotoristaId());
            }
            return alunoRepository.save(existente);
        } catch (DataIntegrityViolationException ex) {
            log.error("Violação de integridade ao atualizar Aluno {}: {}", id, ex.getMostSpecificCause().getMessage());
            throw new DuplicateResourceException("Já existe um aluno com esses dados.");
        }
    }

    public void deleteById(Long id) {
        if (!alunoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Aluno", id);
        }
        alunoRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        return alunoRepository.existsById(id);
    }
}
