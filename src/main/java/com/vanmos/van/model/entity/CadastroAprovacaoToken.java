package com.vanmos.van.model.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Token de uso único do link de aprovação/reprovação de cadastro de
 * motorista, enviado por e-mail ao suporte. Mesmo desenho de
 * PasswordResetToken, mas com validade maior (ver PassageiroService —
 * revisão de documento por humano leva mais tempo que redefinir senha).
 */
@Entity
@Table(name = "cadastro_aprovacao_tokens")
public class CadastroAprovacaoToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, unique = true, length = 100)
    private String token;

    @Column(name = "passageiro_id", nullable = false)
    private Long passageiroId;

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiraEm;

    @Column(name = "usado", nullable = false)
    private boolean usado;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Long getPassageiroId() { return passageiroId; }
    public void setPassageiroId(Long passageiroId) { this.passageiroId = passageiroId; }

    public LocalDateTime getExpiraEm() { return expiraEm; }
    public void setExpiraEm(LocalDateTime expiraEm) { this.expiraEm = expiraEm; }

    public boolean isUsado() { return usado; }
    public void setUsado(boolean usado) { this.usado = usado; }
}
