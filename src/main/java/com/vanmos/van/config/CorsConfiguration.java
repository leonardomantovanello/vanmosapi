package com.vanmos.van.config;

/**
 * DESATIVADO — A configuração de CORS foi migrada para SecurityConfig.java
 * dentro do pacote com.vanmos.van.security.
 *
 * Motivos:
 *  1. @CrossOrigin(origins = "*") nos controllers sobrescrevia esta config.
 *  2. Métodos TRACE e CONNECT foram removidos (vetores de ataque XST).
 *  3. Centralizar CORS no SecurityConfig garante que as regras sejam
 *     aplicadas consistentemente junto com as regras de autenticação.
 *
 * Esta classe pode ser removida com segurança.
 */
public class CorsConfiguration {
    // Intencionalmente vazia — ver SecurityConfig.java
}