package com.vanmos.van.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cadastro")
public class Cadastro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "nome_completo", length = 100, nullable = false)
    private String nomeCompleto;
    
    @Column(name = "idade")
    private Integer idade;
    
    @Column(name = "cpf", length = 14, unique = true)
    private String cpf;
    
    @Column(name = "genero", length = 20)
    private String genero;
    
    @Column(name = "email", length = 100, unique = true)
    private String email;
    
    @Column(name = "senha", length = 255)
    private String senha;
    
    @Column(name = "aceitou_termos")
    private boolean aceitouTermos;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }

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
