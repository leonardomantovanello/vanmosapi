package com.vanmos.van.model.service;

import com.vanmos.van.dto.RotaParadaDTO;
import com.vanmos.van.exception.DuplicateResourceException;
import com.vanmos.van.exception.ForbiddenException;
import com.vanmos.van.exception.ResourceNotFoundException;
import com.vanmos.van.exception.ValidationException;
import com.vanmos.van.model.entity.Aluno;
import com.vanmos.van.model.entity.RotaParada;
import com.vanmos.van.model.repository.RotaParadaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Rota padronizada de um motorista — sequência ordenada dos alunos que ele
 * busca todos os dias. Não guarda endereço próprio: cada parada referencia
 * um Aluno (motorista_id + aluno_id únicos, ver V7__create_rota_paradas.sql),
 * então o endereço mostrado no app vem sempre do cadastro do aluno.
 */
@Service
@Transactional
public class RotaParadaService {

    @Autowired private RotaParadaRepository rotaParadaRepository;
    @Autowired private AlunoService alunoService;

    @Transactional(readOnly = true)
    public List<RotaParadaDTO> listarPorMotorista(Long motoristaId) {
        return rotaParadaRepository.findByMotoristaIdOrderByOrdemAsc(motoristaId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public RotaParadaDTO adicionar(Long motoristaId, Long alunoId) {
        Aluno aluno = alunoService.findById(alunoId)
                .orElseThrow(() -> new ResourceNotFoundException("Aluno", alunoId));

        if (aluno.getMotoristaId() == null || !aluno.getMotoristaId().equals(motoristaId)) {
            throw new ForbiddenException("Você só pode adicionar à rota alunos cadastrados por você.");
        }
        if (rotaParadaRepository.existsByMotoristaIdAndAlunoId(motoristaId, alunoId)) {
            throw new DuplicateResourceException("Este aluno já está na sua rota.");
        }

        RotaParada parada = new RotaParada();
        parada.setMotoristaId(motoristaId);
        parada.setAlunoId(alunoId);
        parada.setOrdem(rotaParadaRepository.maiorOrdem(motoristaId) + 1);
        parada = rotaParadaRepository.save(parada);

        return toDTO(parada, aluno);
    }

    public void remover(Long motoristaId, Long paradaId) {
        RotaParada parada = rotaParadaRepository.findByIdAndMotoristaId(paradaId, motoristaId)
                .orElseThrow(() -> new ResourceNotFoundException("Parada", paradaId));
        rotaParadaRepository.delete(parada);
    }

    // `ids` precisa ser exatamente o conjunto de paradas atuais do motorista
    // (nenhuma faltando, nenhuma de fora) — senão a rota fica inconsistente
    // (algumas paradas sem `ordem` atualizada, outras "perdidas").
    public List<RotaParadaDTO> reordenar(Long motoristaId, List<Long> ids) {
        List<RotaParada> atuais = rotaParadaRepository.findByMotoristaIdOrderByOrdemAsc(motoristaId);
        Set<Long> idsAtuais = atuais.stream().map(RotaParada::getId).collect(Collectors.toSet());
        Set<Long> idsRecebidos = new HashSet<>(ids);

        if (!idsAtuais.equals(idsRecebidos)) {
            throw new ValidationException("A lista precisa conter exatamente as paradas atuais da rota.");
        }

        for (RotaParada parada : atuais) {
            parada.setOrdem(ids.indexOf(parada.getId()) + 1);
        }
        rotaParadaRepository.saveAll(atuais);

        return listarPorMotorista(motoristaId);
    }

    private RotaParadaDTO toDTO(RotaParada parada) {
        Aluno aluno = alunoService.findById(parada.getAlunoId())
                .orElseThrow(() -> new ResourceNotFoundException("Aluno", parada.getAlunoId()));
        return toDTO(parada, aluno);
    }

    private RotaParadaDTO toDTO(RotaParada parada, Aluno aluno) {
        return new RotaParadaDTO(
                parada.getId(),
                aluno.getId(),
                aluno.getNome(),
                aluno.getEnderecoEmbarque(),
                aluno.getEnderecoDesembarque(),
                aluno.getEscola(),
                aluno.getTurno(),
                parada.getOrdem()
        );
    }
}
