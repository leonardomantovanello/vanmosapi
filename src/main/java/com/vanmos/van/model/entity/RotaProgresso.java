package com.vanmos.van.model.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Uma linha por motorista — a parada atual da corrida em andamento (ou
 * ausente/parada_atual_id nula quando não há corrida ativa). Ver
 * RotaController: iniciar() cria/reseta pra primeira parada, avancar() move
 * pra próxima, encerrar() zera parada_atual_id.
 */
@Entity
@Table(name = "rota_progresso")
public class RotaProgresso {

    @Id
    @Column(name = "motorista_id")
    private Long motoristaId;

    @Column(name = "parada_atual_id")
    private Long paradaAtualId;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @PrePersist
    @PreUpdate
    void aoSalvar() {
        atualizadoEm = LocalDateTime.now();
    }

    public Long getMotoristaId() { return motoristaId; }
    public void setMotoristaId(Long motoristaId) { this.motoristaId = motoristaId; }

    public Long getParadaAtualId() { return paradaAtualId; }
    public void setParadaAtualId(Long paradaAtualId) { this.paradaAtualId = paradaAtualId; }

    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}
