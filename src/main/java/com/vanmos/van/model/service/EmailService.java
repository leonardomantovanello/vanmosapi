package com.vanmos.van.model.service;

import com.vanmos.van.dto.ContatoRequest;
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

    /**
     * Encaminha uma submissão do formulário "Contate-nos" do site para a
     * caixa de entrada da VanMos, com Reply-To apontando para o visitante —
     * responder o e-mail já vai direto pra ele, sem precisar copiar o
     * endereço do corpo da mensagem.
     */
    public void enviarContato(ContatoRequest request) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(remetente);
        mensagem.setReplyTo(request.email());
        mensagem.setSubject("Contato pelo site — " + request.assunto());
        mensagem.setText(
                "Nova mensagem recebida pelo formulário Contate-nos:\n\n" +
                "Nome: " + request.nome() + "\n" +
                "E-mail: " + request.email() + "\n" +
                "Telefone: " + (isBlank(request.telefone()) ? "Não informado" : request.telefone()) + "\n" +
                "Assunto: " + request.assunto() + "\n\n" +
                "Mensagem:\n" + request.mensagem()
        );
        try {
            mailSender.send(mensagem);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail de contato de {}", request.email(), e);
            throw new IllegalStateException("Não foi possível enviar sua mensagem agora. Tente novamente em instantes.");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
