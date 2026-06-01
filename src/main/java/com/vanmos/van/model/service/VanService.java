package com.vanmos.van.model.service;

import com.vanmos.van.exception.DuplicateResourceException;
import com.vanmos.van.exception.ResourceNotFoundException;
import com.vanmos.van.model.entity.Van;
import com.vanmos.van.model.repository.VanRepository;
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
public class VanService {

    private static final Logger log = LoggerFactory.getLogger(VanService.class);

    @Autowired
    private VanRepository vanRepository;

    @Transactional(readOnly = true)
    public List<Van> findAll() {
        return vanRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Van> findById(Long id) {
        return vanRepository.findById(id);
    }

    public Van save(Van van) {
        try {
            return vanRepository.save(van);
        } catch (DataIntegrityViolationException ex) {
            log.error("Violação de integridade ao salvar Van: {}", ex.getMostSpecificCause().getMessage());
            throw new DuplicateResourceException("Já existe uma van com esta placa ou RENAVAM.");
        }
    }

    public Van update(Long id, Van van) {
        Van existente = vanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Van", id));
        try {
            // Atualiza apenas campos editáveis — preserva 'ativa' e 'id' originais
            existente.setPlaca(van.getPlaca());
            existente.setModelo(van.getModelo());
            existente.setMarca(van.getMarca());
            existente.setAno(van.getAno());
            existente.setCapacidade(van.getCapacidade());
            existente.setCor(van.getCor());
            existente.setRenavam(van.getRenavam());
            return vanRepository.save(existente);
        } catch (DataIntegrityViolationException ex) {
            log.error("Violação de integridade ao atualizar Van {}: {}", id, ex.getMostSpecificCause().getMessage());
            throw new DuplicateResourceException("Já existe uma van com esta placa ou RENAVAM.");
        }
    }

    public void deleteById(Long id) {
        if (!vanRepository.existsById(id)) {
            throw new ResourceNotFoundException("Van", id);
        }
        vanRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        return vanRepository.existsById(id);
    }
}
