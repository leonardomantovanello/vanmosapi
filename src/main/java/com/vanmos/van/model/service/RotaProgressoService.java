package com.vanmos.van.model.service;

import com.vanmos.van.dto.RotaProgressoDTO;
import com.vanmos.van.exception.ValidationException;
import com.vanmos.van.model.entity.Aluno;
import com.vanmos.van.model.entity.RotaParada;
import com.vanmos.van.model.entity.RotaProgresso;
import com.vanmos.van.model.repository.RotaParadaRepository;
import com.vanmos.van.model.repository.RotaProgressoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Progresso da corrida do motorista pela rota padronizada — sem GPS: o
 * motorista avança manualmente parada por parada, e cada responsável só vê
 * a posição do próprio filho relativa à parada atual (ver
 * obterParaResponsavel — nunca expõe nome/endereço de outro aluno da rota).
 */
@Service
@Transactional
public class RotaProgressoService {

    @Autowired private RotaProgressoRepository rotaProgressoRepository;
    @Autowired private RotaParadaRepository rotaParadaRepository;
    @Autowired private AlunoService alunoService;

    public RotaProgressoDTO iniciar(Long motoristaId) {
        List<RotaParada> paradas = rotaParadaRepository.findByMotoristaIdOrderByOrdemAsc(motoristaId);
        if (paradas.isEmpty()) {
            throw new ValidationException("Cadastre ao menos uma parada na rota antes de começar a corrida.");
        }
        salvarProgresso(motoristaId, paradas.get(0).getId());
        return obterParaMotorista(motoristaId);
    }

    // Avançar na última parada encerra a corrida — evita um passo extra
    // "encerrar" separado no fluxo comum do motorista.
    public RotaProgressoDTO avancar(Long motoristaId) {
        List<RotaParada> paradas = rotaParadaRepository.findByMotoristaIdOrderByOrdemAsc(motoristaId);
        RotaProgresso progresso = rotaProgressoRepository.findById(motoristaId).orElse(null);
        if (progresso == null || progresso.getParadaAtualId() == null) {
            throw new ValidationException("Nenhuma corrida em andamento.");
        }

        int indiceAtual = indiceDaParada(paradas, progresso.getParadaAtualId());
        boolean eraUltima = indiceAtual < 0 || indiceAtual == paradas.size() - 1;
        Long proximaParadaId = eraUltima ? null : paradas.get(indiceAtual + 1).getId();

        salvarProgresso(motoristaId, proximaParadaId);
        return obterParaMotorista(motoristaId);
    }

    public RotaProgressoDTO encerrar(Long motoristaId) {
        salvarProgresso(motoristaId, null);
        return obterParaMotorista(motoristaId);
    }

    @Transactional(readOnly = true)
    public RotaProgressoDTO obterParaMotorista(Long motoristaId) {
        List<RotaParada> paradas = rotaParadaRepository.findByMotoristaIdOrderByOrdemAsc(motoristaId);
        RotaParada atual = paradaAtual(motoristaId, paradas);

        String nomeAtual = atual == null ? null
                : alunoService.findById(atual.getAlunoId()).map(Aluno::getNome).orElse(null);

        return new RotaProgressoDTO(
                atual != null,
                atual != null ? atual.getOrdem() : null,
                paradas.size(),
                nomeAtual,
                null, null, null
        );
    }

    @Transactional(readOnly = true)
    public RotaProgressoDTO obterParaResponsavel(Long responsavelId) {
        Aluno meuAluno = alunoService.findByResponsavelId(responsavelId).stream()
                .filter(aluno -> aluno.getMotoristaId() != null)
                .findFirst()
                .orElse(null);

        if (meuAluno == null) {
            return new RotaProgressoDTO(false, null, 0, null, null, null, null);
        }

        List<RotaParada> paradas = rotaParadaRepository.findByMotoristaIdOrderByOrdemAsc(meuAluno.getMotoristaId());
        RotaParada atual = paradaAtual(meuAluno.getMotoristaId(), paradas);

        Integer suaOrdem = paradas.stream()
                .filter(parada -> parada.getAlunoId().equals(meuAluno.getId()))
                .map(RotaParada::getOrdem)
                .findFirst()
                .orElse(null);

        Integer ordemAtual = atual != null ? atual.getOrdem() : null;
        boolean vocEhAtual = suaOrdem != null && suaOrdem.equals(ordemAtual);
        boolean vocEhOProximo = suaOrdem != null && ordemAtual != null && suaOrdem == ordemAtual + 1;

        return new RotaProgressoDTO(
                atual != null,
                ordemAtual,
                paradas.size(),
                null,
                suaOrdem,
                vocEhAtual,
                vocEhOProximo
        );
    }

    private RotaParada paradaAtual(Long motoristaId, List<RotaParada> paradas) {
        RotaProgresso progresso = rotaProgressoRepository.findById(motoristaId).orElse(null);
        if (progresso == null || progresso.getParadaAtualId() == null) return null;
        return paradas.stream()
                .filter(parada -> parada.getId().equals(progresso.getParadaAtualId()))
                .findFirst()
                .orElse(null);
    }

    private int indiceDaParada(List<RotaParada> paradas, Long paradaId) {
        for (int i = 0; i < paradas.size(); i++) {
            if (paradas.get(i).getId().equals(paradaId)) return i;
        }
        return -1;
    }

    private void salvarProgresso(Long motoristaId, Long paradaAtualId) {
        RotaProgresso progresso = rotaProgressoRepository.findById(motoristaId).orElseGet(RotaProgresso::new);
        progresso.setMotoristaId(motoristaId);
        progresso.setParadaAtualId(paradaAtualId);
        rotaProgressoRepository.save(progresso);
    }
}
