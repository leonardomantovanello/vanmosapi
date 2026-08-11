package com.vanmos.van.model.entity;

/**
 * Estado do cadastro de um motorista no fluxo de aprovação por e-mail (ver
 * PassageiroService#aprovarCadastro/reprovarCadastro). Só é operacional para
 * Passageiro.tipo=MOTORISTA — fica null para PASSAGEIRO.
 */
public enum StatusCadastro {
    PENDENTE,
    APROVADO,
    REPROVADO
}
