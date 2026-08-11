package com.vanmos.van.model.service;

import com.vanmos.van.dto.RotaProgressoDTO;
import com.vanmos.van.exception.ValidationException;
import com.vanmos.van.model.entity.Aluno;
import com.vanmos.van.model.entity.Falta;
import com.vanmos.van.model.entity.RotaParada;
import com.vanmos.van.model.entity.RotaProgresso;
import com.vanmos.van.model.repository.FaltaRepository;
import com.vanmos.van.model.repository.RotaParadaRepository;
import com.vanmos.van.model.repository.RotaProgressoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Progresso da corrida do motorista pela rota padronizada — sem GPS: o
 * motorista avança manualmente parada por parada, e cada responsável só vê
 * a posição do próprio filho relativa à parada atual (ver
 * obterParaResponsavel — nunca expõe nome/endereço de outro aluno da rota).
 *
 * "Rota de hoje" = rota padrão (rota_paradas) menos os alunos com falta
 * registrada pra hoje (ver filtrarAtivasHoje) — a rota padrão em si nunca é
 * alterada, o filtro é só computado aqui. Por isso ordemAtual/suaOrdem/
 * totalParadas no RotaProgressoDTO refletem a posição dentro da lista já
 * filtrada, não o campo "ordem" bruto salvo em RotaParada.
 */
@Service
@Transactional
public class RotaProgressoService {

    @Autowired private RotaProgressoRepository rotaProgressoRepository;
    @Autowired private RotaParadaRepository rotaParadaRepository;
    @Autowired private FaltaRepository faltaRepository;
    @Autowired private AlunoService alunoService;

    public RotaProgressoDTO iniciar(Long motoristaId) {
        List<RotaParada> todas = rotaParadaRepository.findByMotoristaIdOrderByOrdemAsc(motoristaId);
        if (todas.isEmpty()) {
            throw new ValidationException("Cadastre ao menos uma parada na rota antes de começar a corrida.");
        }
        List<RotaParada> ativas = filtrarAtivasHoje(todas);
        if (ativas.isEmpty()) {
            throw new ValidationException("Todos os alunos da rota estão marcados como ausentes hoje.");
        }
        salvarProgresso(motoristaId, ativas.get(0).getId());
        return obterParaMotorista(motoristaId);
    }

    // Avançar na última parada encerra a corrida — evita um passo extra
    // "encerrar" separado no fluxo comum do motorista.
    public RotaProgressoDTO avancar(Long motoristaId) {
        List<RotaParada> paradas = filtrarAtivasHoje(rotaParadaRepository.findByMotoristaIdOrderByOrdemAsc(motoristaId));
        RotaProgresso progresso = rotaProgressoRepository.findById(motoristaId).orElse(null);
        if (progresso == null || progresso.getParadaAtualId() == null) {
            throw new ValidationException("Nenhuma corrida em andamento.");
        }

        // Se a parada atual acabou de ser filtrada (aluno virou ausente
        // depois que o motorista já tinha chegado nela), indiceAtual vem -1
        // e a corrida é tratada como encerrada — nunca sinaliza "buscando"
        // um aluno que acabou de avisar que não vai.
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
        List<RotaParada> paradas = filtrarAtivasHoje(rotaParadaRepository.findByMotoristaIdOrderByOrdemAsc(motoristaId));
        RotaParada atual = paradaAtual(motoristaId, paradas);

        String nomeAtual = atual == null ? null
                : alunoService.findById(atual.getAlunoId()).map(Aluno::getNome).orElse(null);
        Integer posicaoAtual = atual == null ? null : paradas.indexOf(atual) + 1;

        return new RotaProgressoDTO(
                atual != null,
                posicaoAtual,
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

        List<RotaParada> paradas = filtrarAtivasHoje(rotaParadaRepository.findByMotoristaIdOrderByOrdemAsc(meuAluno.getMotoristaId()));
        RotaParada atual = paradaAtual(meuAluno.getMotoristaId(), paradas);

        // Se o próprio filho está ausente hoje, a parada dele não está mais
        // em `paradas` — suaOrdem vem null, reaproveitando o mesmo sinal que
        // já existia pra "não está na rota".
        Integer suaOrdem = null;
        for (int i = 0; i < paradas.size(); i++) {
            if (paradas.get(i).getAlunoId().equals(meuAluno.getId())) {
                suaOrdem = i + 1;
                break;
            }
        }

        Integer ordemAtual = atual != null ? paradas.indexOf(atual) + 1 : null;
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

    // Remove da rota padrão os alunos com falta registrada pra hoje — a
    // rota_paradas em si nunca é alterada, isso é só uma visão computada.
    private List<RotaParada> filtrarAtivasHoje(List<RotaParada> todas) {
        if (todas.isEmpty()) return todas;
        List<Long> alunoIds = todas.stream().map(RotaParada::getAlunoId).toList();
        Set<Long> ausentesHoje = faltaRepository.findByAlunoIdInAndData(alunoIds, LocalDate.now()).stream()
                .map(Falta::getAlunoId)
                .collect(Collectors.toSet());
        return todas.stream().filter(p -> !ausentesHoje.contains(p.getAlunoId())).toList();
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
