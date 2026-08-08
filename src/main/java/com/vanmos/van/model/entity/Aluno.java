package com.vanmos.van.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

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

    // Dígitos apenas (sem parênteses/hífen/espaço) — normalizado antes de
    // salvar (ver PassageiroController.cadastrarPeloMotorista), pra não
    // depender do motorista digitar num formato exato específico.
    @Pattern(regexp = "^\\d{10,11}$", message = "Telefone inválido — informe DDD + número (10 ou 11 dígitos)")
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

    // Mensalidade cobrada pelo motorista por esse aluno — soma dos alunos
    // ativos vira a "Receita Mensal" do dashboard (ver Motorista.jsx).
    // NULL = não informado (opcional no cadastro).
    @DecimalMin(value = "0.0", inclusive = true, message = "Valor mensal não pode ser negativo")
    @Column(name = "valor", precision = 10, scale = 2)
    private BigDecimal valor;

    // FK para passageiros.id — Passageiro é quem de fato autentica como RESPONSAVEL
    // (ver LoginController); a tabela "responsaveis" é um cadastro de perfil
    // separado que nenhum fluxo de login usa hoje.
    @Column(name = "responsavel_id")
    private Long responsavelId;

    // FK para passageiros.id (tipo=MOTORISTA) — o motorista que cadastrou este
    // aluno. Escopa a visibilidade: cada motorista só vê/gerencia os próprios
    // alunos (ver AlunoController), em vez de todos os alunos do sistema.
    @Column(name = "motorista_id")
    private Long motoristaId;

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

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public Long getResponsavelId() { return responsavelId; }
    public void setResponsavelId(Long responsavelId) { this.responsavelId = responsavelId; }

    public Long getMotoristaId() { return motoristaId; }
    public void setMotoristaId(Long motoristaId) { this.motoristaId = motoristaId; }
}