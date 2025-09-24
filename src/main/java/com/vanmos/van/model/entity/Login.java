package com.vanmos.van.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "loguin")
public class Login {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "email_ou_cpf", length = 100, nullable = false)
    private String emailOuCpf;
    
    @Column(name = "senha", length = 255, nullable = false)
    private String senha;
    
    @Column(name = "lembrar_me")
    private boolean lembrarMe;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmailOuCpf() { return emailOuCpf; }
    public void setEmailOuCpf(String emailOuCpf) { this.emailOuCpf = emailOuCpf; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public boolean isLembrarMe() { return lembrarMe; }
    public void setLembrarMe(boolean lembrarMe) { this.lembrarMe = lembrarMe; }
}