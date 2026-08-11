package com.vanmos.van.model.service;

import com.vanmos.van.dto.ContatoRequest;
import com.vanmos.van.model.entity.Passageiro;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remetente;

    // URL pública do site — usada só pra montar o link de redefinição de
    // senha no corpo do e-mail (ver enviarLinkRedefinicaoSenha).
    @Value("${app.frontend-url}")
    private String frontendUrl;

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
     * Envia o link de redefinição de senha em resposta a um pedido de
     * "esqueci minha senha". O token já vem pronto (gerado e persistido por
     * PassageiroService#gerarTokenRedefinicaoSenha) — aqui só monta a URL e
     * manda o e-mail.
     */
    public void enviarLinkRedefinicaoSenha(String destinatario, String nome, String token) {
        String link = frontendUrl + "/redefinir-senha?token=" + token;

        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(destinatario);
        mensagem.setSubject("VanMos — redefinição de senha");
        mensagem.setText(
                "Olá, " + nome + "!\n\n" +
                "Recebemos um pedido de redefinição de senha para sua conta VanMos.\n\n" +
                "Clique no link abaixo para escolher uma nova senha (válido por 30 minutos):\n" +
                link + "\n\n" +
                "Se você não pediu essa redefinição, ignore este e-mail — nada foi alterado na sua conta.\n\n" +
                "Equipe VanMos"
        );
        try {
            mailSender.send(mensagem);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail de link de redefinição para {}", destinatario, e);
            throw new IllegalStateException("Não foi possível enviar o e-mail de redefinição.");
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

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Avisa o suporte (mesma caixa que envia os e-mails — ver enviarContato)
     * que um motorista se cadastrou e está aguardando aprovação. O link leva
     * a uma página pública de revisão com os documentos e os botões reais de
     * Aprovar/Reprovar — o e-mail em si nunca executa a ação diretamente,
     * pra não ser disparado por acidente por scanners de link (Outlook Safe
     * Links e afins, que abrem automaticamente todo link recebido).
     */
    public void enviarSolicitacaoAprovacaoCadastro(Passageiro motorista, String token) {
        String link = frontendUrl + "/motorista/analise-cadastro?token=" + token;

        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(remetente);
        mensagem.setSubject("VanMos — novo cadastro de motorista para aprovação");
        mensagem.setText(
                "Um novo motorista se cadastrou e está aguardando aprovação.\n\n" +
                "Nome: " + motorista.getNome() + "\n" +
                "CPF: " + (isBlank(motorista.getCpf()) ? "Não informado" : motorista.getCpf()) + "\n" +
                "RG: " + (isBlank(motorista.getRg()) ? "Não informado" : motorista.getRg()) + "\n" +
                "CNH: " + (isBlank(motorista.getCnh()) ? "Não informado" : motorista.getCnh()) + "\n" +
                "Telefone: " + (isBlank(motorista.getTelefone()) ? "Não informado" : motorista.getTelefone()) + "\n" +
                "E-mail: " + motorista.getEmail() + "\n" +
                "Data do cadastro: " + motorista.getCriadoEm().format(FORMATO_DATA) + "\n\n" +
                "Para ver os documentos enviados e aprovar ou reprovar este cadastro, acesse:\n" +
                link + "\n\n" +
                "Este link é de uso único e expira em 7 dias.\n\n" +
                "Equipe VanMos"
        );
        try {
            mailSender.send(mensagem);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail de solicitação de aprovação para passageiro id={}", motorista.getId(), e);
            throw new IllegalStateException("Não foi possível notificar o suporte para análise.");
        }
    }

    /** Avisa o motorista que seu cadastro foi aprovado e ele já pode logar. */
    public void enviarConfirmacaoAprovacao(String destinatario, String nome) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(destinatario);
        mensagem.setSubject("VanMos — cadastro aprovado!");
        mensagem.setText(
                "Olá, " + nome + "!\n\n" +
                "Seu cadastro como motorista na VanMos foi aprovado. Você já pode fazer login normalmente:\n\n" +
                frontendUrl + "/login\n\n" +
                "Equipe VanMos"
        );
        try {
            mailSender.send(mensagem);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail de confirmação de aprovação para {}", destinatario, e);
            throw new IllegalStateException("Não foi possível enviar o e-mail de confirmação.");
        }
    }

    /** Avisa o motorista que seu cadastro foi reprovado, com o motivo se houver. */
    public void enviarNotificacaoReprovacao(String destinatario, String nome, String motivo) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(destinatario);
        mensagem.setSubject("VanMos — atualização sobre seu cadastro");
        mensagem.setText(
                "Olá, " + nome + "!\n\n" +
                "Analisamos seu cadastro como motorista na VanMos e, no momento, não foi possível aprová-lo.\n\n" +
                (isBlank(motivo) ? "" : "Motivo informado: " + motivo + "\n\n") +
                "Se tiver dúvidas, entre em contato com o nosso suporte.\n\n" +
                "Equipe VanMos"
        );
        try {
            mailSender.send(mensagem);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail de reprovação para {}", destinatario, e);
            throw new IllegalStateException("Não foi possível enviar o e-mail de notificação.");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
