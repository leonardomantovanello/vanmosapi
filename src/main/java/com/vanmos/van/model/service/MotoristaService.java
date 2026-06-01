package com.vanmos.van.model.service;

import com.vanmos.van.exception.DuplicateResourceException;
import com.vanmos.van.exception.ResourceNotFoundException;
import com.vanmos.van.model.entity.Motorista;
import com.vanmos.van.model.repository.MotoristaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MotoristaService {

    private static final Logger log = LoggerFactory.getLogger(MotoristaService.class);

    @Autowired
    private MotoristaRepository motoristaRepository;

    @Transactional(readOnly = true)
    public List<Motorista> findAll() {
        return motoristaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Motorista> findById(Long id) {
        return motoristaRepository.findById(id);
    }

    public Motorista save(Motorista motorista) {
        try {
            return motoristaRepository.save(motorista);
        } catch (DataIntegrityViolationException ex) {
            log.error("Violação de integridade ao salvar Motorista: {}", ex.getMostSpecificCause().getMessage());
            throw new DuplicateResourceException("Já existe um motorista com este CPF ou CNH.");
        }
    }

    public Motorista update(Long id, Motorista motorista) {
        Motorista existente = motoristaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Motorista", id));
        try {
            // Atualiza apenas campos editáveis — preserva 'ativo', 'cpf' e 'id' originais
            existente.setNome(motorista.getNome());
            existente.setTelefone(motorista.getTelefone());
            existente.setEmail(motorista.getEmail());
            existente.setCnh(motorista.getCnh());
            return motoristaRepository.save(existente);
        } catch (DataIntegrityViolationException ex) {
            log.error("Violação de integridade ao atualizar Motorista {}: {}", id, ex.getMostSpecificCause().getMessage());
            throw new DuplicateResourceException("Já existe um motorista com este CPF ou CNH.");
        }
    }

    public void deleteById(Long id) {
        if (!motoristaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Motorista", id);
        }
        motoristaRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        return motoristaRepository.existsById(id);
    }
}
