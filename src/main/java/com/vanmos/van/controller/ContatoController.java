package com.vanmos.van.controller;

import com.vanmos.van.dto.ApiResponse;
import com.vanmos.van.dto.ContatoRequest;
import com.vanmos.van.model.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Formulário público "Contate-nos" do site — sem autenticação (ver
 * SecurityConfig, "/api/contato" é permitAll); protegido contra abuso pelo
 * RateLimitFilter (5 req/min por IP — ver shouldNotFilter, não é global,
 * é uma allowlist de rotas específicas que inclui esta).
 */
@RestController
@RequestMapping("/api/contato")
public class ContatoController {

    @Autowired private EmailService emailService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> enviar(@Valid @RequestBody ContatoRequest request) {
        emailService.enviarContato(request);
        return ResponseEntity.ok(ApiResponse.noContent("Mensagem enviada com sucesso! Entraremos em contato em breve."));
    }
}
