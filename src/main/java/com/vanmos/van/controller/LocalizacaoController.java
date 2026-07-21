package com.vanmos.van.controller;

import com.vanmos.van.dto.LocalizacaoBroadcast;
import com.vanmos.van.dto.LocalizacaoUpdateRequest;
import com.vanmos.van.security.StompPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * Rastreamento de localização ao vivo do motorista, via STOMP — sem
 * persistência (é efêmero por natureza, não faz sentido guardar histórico
 * de GPS por linha no banco pra esse escopo).
 *
 * motoristaId sempre vem do StompPrincipal autenticado (ver
 * StompAuthInterceptor), nunca do corpo da mensagem — mesmo cuidado de IDOR
 * que AlunoController.criar já tem: um motorista não pode publicar
 * localização em nome de outro só porque sabe o id dele.
 */
@Controller
public class LocalizacaoController {

    @Autowired private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/localizacao")
    public void atualizar(LocalizacaoUpdateRequest request, Principal principal) {
        StompPrincipal motorista = autorizarMotorista(principal);
        if (request.lat() == null || request.lng() == null
                || request.lat() < -90 || request.lat() > 90
                || request.lng() < -180 || request.lng() > 180) {
            return; // payload inválido — ignora silenciosamente, sem derrubar a sessão
        }

        messagingTemplate.convertAndSend(
                "/topic/localizacao/motorista/" + motorista.getUserId(),
                LocalizacaoBroadcast.atualizacao(motorista.getUserId(), request.lat(), request.lng()));
    }

    @MessageMapping("/localizacao/parar")
    public void parar(Principal principal) {
        StompPrincipal motorista = autorizarMotorista(principal);
        messagingTemplate.convertAndSend(
                "/topic/localizacao/motorista/" + motorista.getUserId(),
                LocalizacaoBroadcast.fimDaCorrida(motorista.getUserId()));
    }

    private StompPrincipal autorizarMotorista(Principal principal) {
        if (!(principal instanceof StompPrincipal stompPrincipal) || !"MOTORISTA".equals(stompPrincipal.getRole())) {
            throw new org.springframework.messaging.MessagingException(
                    "Apenas motoristas podem publicar localização.");
        }
        return stompPrincipal;
    }
}
