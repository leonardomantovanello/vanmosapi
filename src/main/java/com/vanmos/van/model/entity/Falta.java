package com.vanmos.van.model.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Falta de um aluno num dia específico — sistema de controle de presença.
 * Um dia SEM registro aqui é considerado normal/presente; só o motorista
 * (nunca o responsável) cria/edita/remove esses registros, sempre com uma
 * justificativa (ver FaltaController). O responsável só visualiza.
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

    // Id do Passageiro (tipo=MOTORISTA) que registrou a falta
    @Column(name = "registrado_por_motorista_id", nullable = false)
    private Long registradoPorMotoristaId;

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

    public Long getRegistradoPorMotoristaId() { return registradoPorMotoristaId; }
    public void setRegistradoPorMotoristaId(Long registradoPorMotoristaId) { this.registradoPorMotoristaId = registradoPorMotoristaId; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}
