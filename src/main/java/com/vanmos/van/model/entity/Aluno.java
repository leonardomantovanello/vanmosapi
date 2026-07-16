package com.vanmos.van.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "alunos")
public class Aluno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    @Pattern(regexp = "^[\\p{L}\\s.'-]+$", message = "Nome contém caracteres inválidos")
    @Column(name = "nome", length = 100, nullable = false)
    private String nome;

    @Pattern(regexp = "^(\\(\\d{2}\\)\\s?)?\\d{4,5}-?\\d{4}$",
             message = "Telefone inválido")
    @Column(name = "telefone_responsavel", length = 15)
    private String telefoneResponsavel;

    @Size(max = 200, message = "Endereço de embarque deve ter no máximo 200 caracteres")
    @Column(name = "endereco_embarque", length = 200)
    private String enderecoEmbarque;

    @Size(max = 200, message = "Endereço de desembarque deve ter no máximo 200 caracteres")
    @Column(name = "endereco_desembarque", length = 200)
    private String enderecoDesembarque;

    @Size(max = 100, message = "Escola deve ter no máximo 100 caracteres")
    @Column(name = "escola", length = 100)
    private String escola;

    @Pattern(regexp = "^(MANHA|TARDE|NOITE|manha|tarde|noite|Manhã|Tarde|Noite)$",
             message = "Turno deve ser: MANHA, TARDE ou NOITE")
    @Column(name = "turno", length = 20)
    private String turno;

    @Column(name = "ativo")
    private boolean ativo;

    // FK para cadastro.id — Cadastro é quem de fato autentica como RESPONSAVEL
    // (ver LoginController); a tabela "responsaveis" é um cadastro de perfil
    // separado que nenhum fluxo de login usa hoje.
    @Column(name = "responsavel_id")
    private Long responsavelId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getTelefoneResponsavel() { return telefoneResponsavel; }
    public void setTelefoneResponsavel(String telefoneResponsavel) { this.telefoneResponsavel = telefoneResponsavel; }

    public String getEnderecoEmbarque() { return enderecoEmbarque; }
    public void setEnderecoEmbarque(String enderecoEmbarque) { this.enderecoEmbarque = enderecoEmbarque; }

    public String getEnderecoDesembarque() { return enderecoDesembarque; }
    public void setEnderecoDesembarque(String enderecoDesembarque) { this.enderecoDesembarque = enderecoDesembarque; }

    public String getEscola() { return escola; }
    public void setEscola(String escola) { this.escola = escola; }

    public String getTurno() { return turno; }
    public void setTurno(String turno) { this.turno = turno; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public Long getResponsavelId() { return responsavelId; }
    public void setResponsavelId(Long responsavelId) { this.responsavelId = responsavelId; }
}