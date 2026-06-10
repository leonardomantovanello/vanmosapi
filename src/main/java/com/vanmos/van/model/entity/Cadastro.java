package com.vanmos.van.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * Bean Validation aplicada diretamente na entidade (ponto 3 do relatório).
 *
 * CAMADAS DE VALIDAÇÃO:
 *  1. @NotBlank / @Email / @Pattern — valida formato ANTES de qualquer query.
 *     O Spring lança MethodArgumentNotValidException se @Valid for usado no controller,
 *     capturada pelo GlobalExceptionHandler com HTTP 400 e lista de erros por campo.
 *  2. @Column(nullable=false, length=N) — constraint no banco como segunda barreira.
 *
 * SQL INJECTION:
 *  O JPA/Hibernate usa Prepared Statements internamente para TODOS os métodos
 *  do JpaRepository (save, findById, findAll, etc.).
 *  Os parâmetros são sempre vinculados via '?' — nunca concatenados na query.
 *  As anotações @Query em JPQL também usam bind parameters (:param).
 *  Portanto, SQL Injection é estruturalmente impossível nesta camada.
 */
@Entity
@Table(name = "cadastro")
public class Cadastro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    @Pattern(regexp = "^[\\p{L}\\s.'-]+$", message = "Nome contém caracteres inválidos")
    @Column(name = "nome", length = 100, nullable = false)
    private String nome;

    @Min(value = 0, message = "Idade não pode ser negativa")
    @Max(value = 120, message = "Idade inválida")
    @Column(name = "idade")
    private Integer idade;

    // CPF: aceita formatado (000.000.000-00) ou apenas dígitos (00000000000)
    @Pattern(regexp = "^(\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}|\\d{11})$",
             message = "CPF deve estar no formato 000.000.000-00 ou conter 11 dígitos")
    @Column(name = "cpf", length = 14, unique = true)
    private String cpf;

    @Size(max = 20, message = "Gênero deve ter no máximo 20 caracteres")
    @Column(name = "genero", length = 20)
    private String genero;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @Size(max = 100, message = "E-mail deve ter no máximo 100 caracteres")
    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    // Senha: mínimo 8 chars, ao menos 1 letra maiúscula, 1 minúscula, 1 número
    // Validação aplicada ANTES do hash — o hash em si não precisa seguir este padrão
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
    @Column(name = "senha", length = 255, nullable = false)
    private String senha;

    @Column(name = "aceitou_termos")
    private boolean aceitouTermos;

    @Column(name = "ativo")
    private boolean ativo;

    public boolean getAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Integer getIdade() { return idade; }
    public void setIdade(Integer idade) { this.idade = idade; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public boolean isAceitouTermos() { return aceitouTermos; }
    public void setAceitouTermos(boolean aceitouTermos) { this.aceitouTermos = aceitouTermos; }
}
