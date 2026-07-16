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

    // Upsert: se já existe falta nesse dia, só atualiza a justificativa (não
    // deixa duplicar registro pro mesmo aluno+dia — UQ_faltas_aluno_data).
    @Transactional
    public Falta marcar(Long alunoId, LocalDate data, String justificativa, Long registradoPorMotoristaId) {
        Falta falta = faltaRepository.findByAlunoIdAndData(alunoId, data).orElseGet(Falta::new);
        falta.setAlunoId(alunoId);
        falta.setData(data);
        falta.setJustificativa(justificativa);
        falta.setRegistradoPorMotoristaId(registradoPorMotoristaId);
        return faltaRepository.save(falta);
    }

    @Transactional
    public void desmarcar(Long alunoId, LocalDate data) {
        faltaRepository.deleteByAlunoIdAndData(alunoId, data);
    }
}
