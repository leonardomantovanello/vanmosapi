package com.vanmos.van.model.service;

import com.vanmos.van.exception.DuplicateResourceException;
import com.vanmos.van.exception.ResourceNotFoundException;
import com.vanmos.van.model.entity.Responsavel;
import com.vanmos.van.model.repository.ResponsavelRepository;
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
public class ResponsavelService {

    private static final Logger log = LoggerFactory.getLogger(ResponsavelService.class);

    @Autowired
    private ResponsavelRepository responsavelRepository;

    @Transactional(readOnly = true)
    public List<Responsavel> findAll() {
        return responsavelRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Responsavel> findById(Long id) {
        return responsavelRepository.findById(id);
    }

    public Responsavel save(Responsavel responsavel) {
        try {
            return responsavelRepository.save(responsavel);
        } catch (DataIntegrityViolationException ex) {
            log.error("Violação de integridade ao salvar Responsavel: {}", ex.getMostSpecificCause().getMessage());
            throw new DuplicateResourceException("Já existe um responsável com este CPF ou e-mail.");
        }
    }

    public Responsavel update(Long id, Responsavel responsavel) {
        if (!responsavelRepository.existsById(id)) {
            throw new ResourceNotFoundException("Responsavel", id);
        }
        try {
            responsavel.setId(id);
            return responsavelRepository.save(responsavel);
        } catch (DataIntegrityViolationException ex) {
            log.error("Violação de integridade ao atualizar Responsavel {}: {}", id, ex.getMostSpecificCause().getMessage());
            throw new DuplicateResourceException("Já existe um responsável com este CPF ou e-mail.");
        }
    }

    public void deleteById(Long id) {
        if (!responsavelRepository.existsById(id)) {
            throw new ResourceNotFoundException("Responsavel", id);
        }
        responsavelRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        return responsavelRepository.existsById(id);
    }
}
