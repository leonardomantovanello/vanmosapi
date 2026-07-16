package com.vanmos.van.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

/**
 * Mensagem de chat entre um motorista e o responsável de um aluno —
 * persistida no banco (antes vivia só em memória no app, MOCK_MESSAGES).
 * A conversa é escopada por aluno (alunoId): tanto o motorista quanto o
 * responsável daquele aluno enxergam a mesma thread (ver MensagemController).
 */
@Entity
@Table(name = "mensagens")
public class Mensagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aluno_id", nullable = false)
    private Long alunoId;

    // Quem mandou — MOTORISTA ou RESPONSAVEL. Guardado explicitamente (em vez
    // de inferido comparando remetenteId com aluno.motoristaId/responsavelId
    // toda vez) porque simplifica a exibição no app (bolha própria vs do
    // contato) sem precisar recarregar o Aluno a cada mensagem.
    @Column(name = "remetente_tipo", length = 20, nullable = false)
    private String remetenteTipo;

    // Id do Passageiro (motorista ou responsável) que enviou
    @Column(name = "remetente_id", nullable = false)
    private Long remetenteId;

    @NotBlank(message = "Texto é obrigatório")
    @Column(name = "texto", length = 2000, nullable = false)
    private String texto;

    // LocalDateTime (não Instant) porque a coluna é DATETIME2 (sem timezone)
    // — Instant exigiria DATETIMEOFFSET no SQL Server e quebra a validação
    // de schema do Hibernate (ddl-auto=validate) contra a migration V5.
    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    void aoPersistir() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAlunoId() { return alunoId; }
    public void setAlunoId(Long alunoId) { this.alunoId = alunoId; }

    public String getRemetenteTipo() { return remetenteTipo; }
    public void setRemetenteTipo(String remetenteTipo) { this.remetenteTipo = remetenteTipo; }

    public Long getRemetenteId() { return remetenteId; }
    public void setRemetenteId(Long remetenteId) { this.remetenteId = remetenteId; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}
