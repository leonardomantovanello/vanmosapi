package com.vanmos.van.dto;

import com.vanmos.van.model.entity.MotoristasAdmin;

/**
 * Recorte público de MotoristasAdmin — sem cpf/cnh/gmail/senha.
 * Usado na página pública "Nossos Motoristas", que não exige login.
 */
public record MotoristaPublicoDTO(Long id, String nomeCompleto, String modeloVan, String placaVan, boolean ativo) {

    public static MotoristaPublicoDTO from(MotoristasAdmin m) {
        return new MotoristaPublicoDTO(m.getId(), m.getNomeCompleto(), m.getModeloVan(), m.getPlacaVan(), m.isAtivo());
    }
}
