package com.vanmos.van.model.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Token de uso único do fluxo "esqueci minha senha" por link de e-mail —
 * mesmo desenho de PasswordResetToken, só que pra Motorista. Tabela própria
 * porque password_reset_tokens (Passageiro) e motorista deixaram de
 * compartilhar id space na migração de V15 (ver MotoristaService).
 */
@Entity
@Table(name = "motorista_password_reset_tokens")
public class MotoristaPasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, unique = true, length = 100)
    private String token;

    @Column(name = "motorista_id", nullable = false)
    private Long motoristaId;

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiraEm;

    @Column(name = "usado", nullable = false)
    private boolean usado;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Long getMotoristaId() { return motoristaId; }
    public void setMotoristaId(Long motoristaId) { this.motoristaId = motoristaId; }

    public LocalDateTime getExpiraEm() { return expiraEm; }
    public void setExpiraEm(LocalDateTime expiraEm) { this.expiraEm = expiraEm; }

    public boolean isUsado() { return usado; }
    public void setUsado(boolean usado) { this.usado = usado; }
}
