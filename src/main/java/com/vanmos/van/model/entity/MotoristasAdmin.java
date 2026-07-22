package com.vanmos.van.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "motoristas_admin")
public class MotoristasAdmin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "cnh")
    private String cnh;
    
    @Column(name = "cpf")
    private String cpf;
    
    @Column(name = "gmail")
    private String gmail;
    
    @Column(name = "modelo_van")
    private String modeloVan;
    
    @Column(name = "nome_completo")
    private String nomeCompleto;
    
    @Column(name = "placa_van")
    private String placaVan;
    
    @Column(name = "senha")
    private String senha;
    
    @Column(name = "ativo")
    private boolean ativo = true;

    // Foto de perfil como data URI base64 ("data:image/jpeg;base64,...") —
    // sem storage de arquivos configurado no projeto (ver V9). NULL é um
    // estado válido (sem foto).
    @Column(name = "avatar_base64", columnDefinition = "NVARCHAR(MAX)")
    private String avatarBase64;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCnh() { return cnh; }
    public void setCnh(String cnh) { this.cnh = cnh; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getGmail() { return gmail; }
    public void setGmail(String gmail) { this.gmail = gmail; }

    public String getModeloVan() { return modeloVan; }
    public void setModeloVan(String modeloVan) { this.modeloVan = modeloVan; }

    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }

    public String getPlacaVan() { return placaVan; }
    public void setPlacaVan(String placaVan) { this.placaVan = placaVan; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public String getAvatarBase64() { return avatarBase64; }
    public void setAvatarBase64(String avatarBase64) { this.avatarBase64 = avatarBase64; }
}