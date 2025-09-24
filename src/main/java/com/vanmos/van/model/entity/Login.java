package com.vanmos.van.model.entity;

public class Login {
    private String emailOuCpf;
    private String senha;
    private boolean lembrarMe;

    public String getEmailOuCpf() { return emailOuCpf; }
    public void setEmailOuCpf(String emailOuCpf) { this.emailOuCpf = emailOuCpf; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public boolean isLembrarMe() { return lembrarMe; }
    public void setLembrarMe(boolean lembrarMe) { this.lembrarMe = lembrarMe; }
}