package com.vanmos.van.controller;

import com.vanmos.van.dto.AdicionarParadaRequest;
import com.vanmos.van.dto.ApiResponse;
import com.vanmos.van.dto.ReordenarRotaRequest;
import com.vanmos.van.dto.RotaParadaDTO;
import com.vanmos.van.dto.RotaProgressoDTO;
import com.vanmos.van.exception.ForbiddenException;
import com.vanmos.van.model.service.RotaParadaService;
import com.vanmos.van.model.service.RotaProgressoService;
import com.vanmos.van.security.JwtUtil;
import com.vanmos.van.security.OwnershipValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Rota padronizada do motorista logado — sequência de paradas (alunos) que
 * ele segue todos os dias. Só o próprio motorista gerencia a sua rota; não
 * existe visão "rota de outro motorista" aqui (ver validarMotorista).
 */
@RestController
@RequestMapping("/api/rotas")
public class RotaController {

    @Autowired private RotaParadaService rotaParadaService;
    @Autowired private RotaProgressoService rotaProgressoService;
    @Autowired private OwnershipValidator ownership;
    @Autowired private JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RotaParadaDTO>>> listar() {
        Long motoristaId = validarMotorista();
        return ResponseEntity.ok(ApiResponse.ok("Rota listada.", rotaParadaService.listarPorMotorista(motoristaId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RotaParadaDTO>> adicionar(@Valid @RequestBody AdicionarParadaRequest request) {
        Long motoristaId = validarMotorista();
        RotaParadaDTO parada = rotaParadaService.adicionar(motoristaId, request.alunoId());
        return ResponseEntity.status(201).body(ApiResponse.created("Parada adicionada à rota.", parada));
    }

    @PutMapping("/reordenar")
    public ResponseEntity<ApiResponse<List<RotaParadaDTO>>> reordenar(@Valid @RequestBody ReordenarRotaRequest request) {
        Long motoristaId = validarMotorista();
        List<RotaParadaDTO> rota = rotaParadaService.reordenar(motoristaId, request.ids());
        return ResponseEntity.ok(ApiResponse.ok("Rota reordenada.", rota));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> remover(@PathVariable Long id) {
        Long motoristaId = validarMotorista();
        rotaParadaService.remover(motoristaId, id);
        return ResponseEntity.ok(ApiResponse.noContent("Parada removida da rota."));
    }

    // MOTORISTA vê a própria corrida (parada atual + nome do aluno);
    // RESPONSAVEL vê só a posição do próprio filho relativa a essa parada
    // (ver RotaProgressoService.obterParaResponsavel — nunca expõe dados de
    // outro aluno da rota).
    @GetMapping("/progresso")
    public ResponseEntity<ApiResponse<RotaProgressoDTO>> progresso() {
        String role = ownership.getCurrentRole();
        Long currentUserId = ownership.getCurrentUserId(jwtUtil);

        if ("MOTORISTA".equals(role)) {
            return ResponseEntity.ok(ApiResponse.ok("Progresso da rota.", rotaProgressoService.obterParaMotorista(currentUserId)));
        }
        if ("RESPONSAVEL".equals(role)) {
            return ResponseEntity.ok(ApiResponse.ok("Progresso da rota.", rotaProgressoService.obterParaResponsavel(currentUserId)));
        }
        throw new ForbiddenException("Apenas motoristas e responsáveis acompanham o progresso da rota.");
    }

    @PostMapping("/progresso/iniciar")
    public ResponseEntity<ApiResponse<RotaProgressoDTO>> iniciarProgresso() {
        Long motoristaId = validarMotorista();
        return ResponseEntity.ok(ApiResponse.ok("Corrida iniciada.", rotaProgressoService.iniciar(motoristaId)));
    }

    @PostMapping("/progresso/avancar")
    public ResponseEntity<ApiResponse<RotaProgressoDTO>> avancarProgresso() {
        Long motoristaId = validarMotorista();
        return ResponseEntity.ok(ApiResponse.ok("Próxima parada.", rotaProgressoService.avancar(motoristaId)));
    }

    @PostMapping("/progresso/encerrar")
    public ResponseEntity<ApiResponse<RotaProgressoDTO>> encerrarProgresso() {
        Long motoristaId = validarMotorista();
        return ResponseEntity.ok(ApiResponse.ok("Corrida encerrada.", rotaProgressoService.encerrar(motoristaId)));
    }

    // A rota é sempre a do motorista autenticado — nunca aceitar um
    // motoristaId vindo de query/body, senão um motorista poderia ler/editar
    // a rota de outro só trocando o id na requisição (IDOR).
    private Long validarMotorista() {
        if (!"MOTORISTA".equals(ownership.getCurrentRole())) {
            throw new ForbiddenException("Apenas motoristas têm rota padronizada.");
        }
        return ownership.getCurrentUserId(jwtUtil);
    }
}
