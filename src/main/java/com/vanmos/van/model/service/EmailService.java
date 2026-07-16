package com.vanmos.van.model.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remetente;

    /**
     * Envia a senha gerada para um passageiro cadastrado por um motorista.
     * Falha de envio não deve impedir o cadastro em si (a conta já foi
     * criada) — o chamador decide como avisar o motorista se o e-mail falhar.
     */
    public void enviarSenhaGerada(String destinatario, String nome, String senha) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(destinatario);
        mensagem.setSubject("VanMos — seu acesso ao aplicativo");
        mensagem.setText(
                "Olá, " + nome + "!\n\n" +
                "Seu motorista cadastrou você no aplicativo VanMos.\n\n" +
                "Use estes dados para entrar no app (opção Passageiro):\n" +
                "E-mail: " + destinatario + "\n" +
                "Senha: " + senha + "\n\n" +
                "Recomendamos trocar essa senha assim que possível.\n\n" +
                "Equipe VanMos"
        );
        try {
            mailSender.send(mensagem);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail de senha gerada para {}", destinatario, e);
            throw new IllegalStateException("Não foi possível enviar o e-mail com a senha. Tente novamente.");
        }
    }
}
