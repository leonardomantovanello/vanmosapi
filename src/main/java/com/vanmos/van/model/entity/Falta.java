package com.vanmos.van.model.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Falta de um aluno num dia específico — sistema de controle de presença.
 * Um dia SEM registro aqui é considerado normal/presente; só o responsável
 * (nunca o motorista) cria/edita/remove esses registros — quem avisa que o
 * aluno não vai é quem sabe disso primeiro (ver FaltaController). Marcar
 * uma falta para hoje remove a parada da corrida do motorista (ver
 * RotaProgressoService). O motorista só visualiza (ex.: etiqueta "Faltou
 * hoje" na lista de passageiros).
 */
@Entity
@Table(
    name = "faltas",
    uniqueConstraints = @UniqueConstraint(name = "UQ_faltas_aluno_data", columnNames = {"aluno_id", "data"})
)
public class Falta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aluno_id", nullable = false)
    private Long alunoId;

    @Column(name = "data", nullable = false)
    private LocalDate data;

    @Column(name = "justificativa", length = 500)
    private String justificativa;

    // Id do Passageiro (tipo=RESPONSAVEL, ou ADMIN em casos excepcionais) que registrou a falta
    @Column(name = "registrado_por_id", nullable = false)
    private Long registradoPorId;

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

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public String getJustificativa() { return justificativa; }
    public void setJustificativa(String justificativa) { this.justificativa = justificativa; }

    public Long getRegistradoPorId() { return registradoPorId; }
    public void setRegistradoPorId(Long registradoPorId) { this.registradoPorId = registradoPorId; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}
