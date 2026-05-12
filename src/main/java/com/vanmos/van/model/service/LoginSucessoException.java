package com.vanmos.van.model.service;

import com.vanmos.van.model.entity.Cadastro;

public class LoginSucessoException extends RuntimeException {
    private final Cadastro usuario;

    public LoginSucessoException(Cadastro usuario) {
        super("Login realizado com sucesso");
        this.usuario = usuario;
    }

    public Cadastro getUsuario() {
        return usuario;
    }
}
