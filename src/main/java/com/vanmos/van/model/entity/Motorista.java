package com.vanmos.van.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

/**
 * Conta de motorista — login, senha e todo o fluxo de aprovação por e-mail
 * vivem aqui (migrado de Passageiro/tipo=MOTORISTA, ver V14/V15). Motorista
 * é uma tabela só de motorista — sem discriminador de tipo, ao contrário de
 * Passageiro, que continua servindo só responsável/passageiro.
 *
 * Bean Validation aplicada direto na entidade, mesmo padrão de Passageiro —
 * só entra em vigor em endpoints que usam @Valid (cadastrar()); edição
 * parcial (atualizar()) não usa @Valid de propósito.
 */
@Entity
@Table(name = "motorista")
public class Motorista {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    @Pattern(regexp = "^[\\p{L}\\s.'-]+$", message = "Nome contém caracteres inválidos")
    @Column(name = "nome", length = 100, nullable = false)
    private String nome;

    // CPF: aceita formatado (000.000.000-00) ou apenas dígitos (00000000000)
    @Pattern(regexp = "^(\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}|\\d{11})$",
             message = "CPF deve estar no formato 000.000.000-00 ou conter 11 dígitos")
    @Column(name = "cpf", length = 14, unique = true)
    private String cpf;

    @Column(name = "cnh", length = 20, unique = true)
    private String cnh;

    @Min(value = 0, message = "Idade não pode ser negativa")
    @Max(value = 120, message = "Idade inválida")
    @Column(name = "idade")
    private Integer idade;

    @Size(max = 20, message = "Gênero deve ter no máximo 20 caracteres")
    @Column(name = "genero", length = 20)
    private String genero;

    @Column(name = "telefone", length = 15)
    private String telefone;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @Size(max = 100, message = "E-mail deve ter no máximo 100 caracteres")
    @Column(name = "email", length = 100, unique = true)
    private String email;

    @Column(name = "ativo")
    private boolean ativo;

    // Senha: mínimo 8 chars, ao menos 1 letra maiúscula, 1 minúscula, 1 número
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
    @Column(name = "senha", length = 255)
    private String senha;

    @Column(name = "aceito_termos")
    private boolean aceitoTermos;

    @Column(name = "rg", length = 20)
    private String rg;

    // Documentos como data URI base64, mesmo padrão de avatarBase64 — sem
    // storage de arquivos configurado no projeto.
    @Column(name = "rg_documento_base64", columnDefinition = "NVARCHAR(MAX)")
    private String rgDocumentoBase64;

    @Column(name = "cnh_documento_base64", columnDefinition = "NVARCHAR(MAX)")
    private String cnhDocumentoBase64;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_cadastro", length = 20)
    private StatusCadastro statusCadastro;

    @Column(name = "motivo_reprovacao", columnDefinition = "NVARCHAR(MAX)")
    private String motivoReprovacao;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    // Foto de perfil como data URI base64 — mesmo padrão de Passageiro.
    @Column(name = "avatar_base64", columnDefinition = "NVARCHAR(MAX)")
    private String avatarBase64;

    // Só existiam em MotoristasAdmin (tabela apagada, ver V15) — necessários
    // pra página pública "Nossos Motoristas". NULL até o motorista
    // preencher no próprio perfil (não fazem parte do autocadastro).
    @Column(name = "modelo_van", length = 100)
    private String modeloVan;

    @Column(name = "placa_van", length = 20)
    private String placaVan;

    @PrePersist
    private void aoPersistir() {
        if (criadoEm == null) criadoEm = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getCnh() { return cnh; }
    public void setCnh(String cnh) { this.cnh = cnh; }

    public Integer getIdade() { return idade; }
    public void setIdade(Integer idade) { this.idade = idade; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public boolean isAceitoTermos() { return aceitoTermos; }
    public void setAceitoTermos(boolean aceitoTermos) { this.aceitoTermos = aceitoTermos; }

    public String getRg() { return rg; }
    public void setRg(String rg) { this.rg = rg; }

    public String getRgDocumentoBase64() { return rgDocumentoBase64; }
    public void setRgDocumentoBase64(String rgDocumentoBase64) { this.rgDocumentoBase64 = rgDocumentoBase64; }

    public String getCnhDocumentoBase64() { return cnhDocumentoBase64; }
    public void setCnhDocumentoBase64(String cnhDocumentoBase64) { this.cnhDocumentoBase64 = cnhDocumentoBase64; }

    public StatusCadastro getStatusCadastro() { return statusCadastro; }
    public void setStatusCadastro(StatusCadastro statusCadastro) { this.statusCadastro = statusCadastro; }

    public String getMotivoReprovacao() { return motivoReprovacao; }
    public void setMotivoReprovacao(String motivoReprovacao) { this.motivoReprovacao = motivoReprovacao; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }

    public String getAvatarBase64() { return avatarBase64; }
    public void setAvatarBase64(String avatarBase64) { this.avatarBase64 = avatarBase64; }

    public String getModeloVan() { return modeloVan; }
    public void setModeloVan(String modeloVan) { this.modeloVan = modeloVan; }

    public String getPlacaVan() { return placaVan; }
    public void setPlacaVan(String placaVan) { this.placaVan = placaVan; }
}
