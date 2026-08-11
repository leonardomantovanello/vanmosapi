package com.vanmos.van.model.service;

import com.vanmos.van.model.entity.Falta;
import com.vanmos.van.model.repository.FaltaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class FaltaService {

    @Autowired
    private FaltaRepository faltaRepository;

    public List<Falta> listarPorAluno(Long alunoId) {
        return faltaRepository.findByAlunoIdOrderByDataAsc(alunoId);
    }

    // Usado pelo motorista pra saber quem da rota está ausente hoje (etiqueta
    // "Faltou hoje" + notificação local — ver FaltaController#hoje).
    public List<Falta> listarPorAlunosEData(List<Long> alunoIds, LocalDate data) {
        if (alunoIds.isEmpty()) return List.of();
        return faltaRepository.findByAlunoIdInAndData(alunoIds, data);
    }

    // Upsert: se já existe falta nesse dia, só atualiza a justificativa (não
    // deixa duplicar registro pro mesmo aluno+dia — UQ_faltas_aluno_data).
    @Transactional
    public Falta marcar(Long alunoId, LocalDate data, String justificativa, Long registradoPorId) {
        Falta falta = faltaRepository.findByAlunoIdAndData(alunoId, data).orElseGet(Falta::new);
        falta.setAlunoId(alunoId);
        falta.setData(data);
        falta.setJustificativa(justificativa);
        falta.setRegistradoPorId(registradoPorId);
        return faltaRepository.save(falta);
    }

    @Transactional
    public void desmarcar(Long alunoId, LocalDate data) {
        faltaRepository.deleteByAlunoIdAndData(alunoId, data);
    }
}
