package com.vanmos.van.dto;

import java.time.Instant;

/**
 * Publicado em /topic/localizacao/motorista/{motoristaId}. "sharing" false
 * marca o fim da corrida — permite ao app do responsável distinguir "van
 * parada de propósito" de "sem sinal no momento".
 */
public record LocalizacaoBroadcast(Long motoristaId, Double lat, Double lng, boolean sharing, Instant timestamp) {

    public static LocalizacaoBroadcast atualizacao(Long motoristaId, Double lat, Double lng) {
        return new LocalizacaoBroadcast(motoristaId, lat, lng, true, Instant.now());
    }

    public static LocalizacaoBroadcast fimDaCorrida(Long motoristaId) {
        return new LocalizacaoBroadcast(motoristaId, null, null, false, Instant.now());
    }
}
