package com.vanmos.van.dto;

import com.vanmos.van.model.entity.Motorista;

/**
 * Recorte público de Motorista — sem cpf/cnh/email/senha/documentos.
 * Usado na página pública "Nossos Motoristas" (site, sem login) e pela tela
 * "Seu motorista" do responsável no app (que precisa da foto do motorista
 * mas não tem acesso ao GET /api/motoristas/{id} completo, restrito por
 * ownership ao próprio motorista/ADMIN). avatarBase64 é uma foto de perfil,
 * mesmo nível de exposição pública que nome/modelo/placa da van já tinham.
 * Chave "nomeCompleto" preservada (em vez de "nome") pra Motoristas.jsx no
 * site não precisar mudar nada mesmo com a migração de MotoristasAdmin pra
 * Motorista por trás.
 */
public record MotoristaPublicoDTO(
        Long id, String nomeCompleto, String modeloVan, String placaVan, boolean ativo, String avatarBase64) {

    public static MotoristaPublicoDTO from(Motorista m) {
        return new MotoristaPublicoDTO(
                m.getId(), m.getNome(), m.getModeloVan(), m.getPlacaVan(), m.isAtivo(), m.getAvatarBase64());
    }
}
