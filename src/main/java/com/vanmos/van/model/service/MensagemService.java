package com.vanmos.van.model.service;

import com.vanmos.van.model.entity.Mensagem;
import com.vanmos.van.model.repository.MensagemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MensagemService {

    @Autowired
    private MensagemRepository mensagemRepository;

    public List<Mensagem> listarPorAluno(Long alunoId) {
        return mensagemRepository.findByAlunoIdOrderByCriadoEmAsc(alunoId);
    }

    public Mensagem enviar(Long alunoId, String remetenteTipo, Long remetenteId, String texto) {
        Mensagem mensagem = new Mensagem();
        mensagem.setAlunoId(alunoId);
        mensagem.setRemetenteTipo(remetenteTipo);
        mensagem.setRemetenteId(remetenteId);
        mensagem.setTexto(texto);
        return mensagemRepository.save(mensagem);
    }
}
