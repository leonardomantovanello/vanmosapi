package com.vanmos.van.model.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Uma parada na rota padronizada de um motorista — referencia um aluno já
 * cadastrado (endereço vem de Aluno.enderecoEmbarque/enderecoDesembarque) e
 * guarda só a posição dele na sequência da rota (ver RotaController).
 */
@Entity
@Table(
    name = "rota_paradas",
    uniqueConstraints = @UniqueConstraint(name = "UQ_rota_paradas_motorista_aluno", columnNames = {"motorista_id", "aluno_id"})
)
public class RotaParada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "motorista_id", nullable = false)
    private Long motoristaId;

    @Column(name = "aluno_id", nullable = false)
    private Long alunoId;

    @Column(name = "ordem", nullable = false)
    private Integer ordem;

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

    public Long getMotoristaId() { return motoristaId; }
    public void setMotoristaId(Long motoristaId) { this.motoristaId = motoristaId; }

    public Long getAlunoId() { return alunoId; }
    public void setAlunoId(Long alunoId) { this.alunoId = alunoId; }

    public Integer getOrdem() { return ordem; }
    public void setOrdem(Integer ordem) { this.ordem = ordem; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}
