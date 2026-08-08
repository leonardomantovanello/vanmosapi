package com.vanmos.van.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testa a peça central da autenticação da API: geração e validação de JWT.
 * Sem @SpringBootTest de propósito — JwtUtil não depende de mais nada além
 * dos valores de @Value, então injetamos eles direto via ReflectionTestUtils
 * e o teste roda em milissegundos, sem subir contexto Spring nem banco.
 */
class JwtUtilTest {

    // Só precisa ser longa o bastante pra HMAC-SHA512 (>= 64 bytes) — não
    // precisa ser o segredo real, é isolado por teste.
    private static final String TEST_SECRET =
            "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "accessExpirationMs", 900_000L);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpirationMs", 604_800_000L);
    }

    @Test
    void tokenDeAcessoValidoDeveSerAceitoEConterAsClaimsCorretas() {
        String token = jwtUtil.generateAccessToken("motorista@vanmos.com", "MOTORISTA", 42L);

        assertTrue(jwtUtil.isValidAccessToken(token));
        assertEquals("motorista@vanmos.com", jwtUtil.extractSubject(token));
        assertEquals("MOTORISTA", jwtUtil.extractRole(token));
        assertEquals(42L, jwtUtil.extractUserId(token));
    }

    @Test
    void refreshTokenNaoDeveSerAceitoComoAccessToken() {
        // Protege contra um refresh token (vida longa, cookie) ser usado
        // pra autenticar rotas — deve valer só pra emitir access token novo.
        String refreshToken = jwtUtil.generateRefreshToken("motorista@vanmos.com");

        assertFalse(jwtUtil.isValidAccessToken(refreshToken));
        assertTrue(jwtUtil.isValidRefreshToken(refreshToken));
    }

    @Test
    void accessTokenNaoDeveSerAceitoComoRefreshToken() {
        String accessToken = jwtUtil.generateAccessToken("motorista@vanmos.com", "MOTORISTA", 1L);

        assertFalse(jwtUtil.isValidRefreshToken(accessToken));
    }

    @Test
    void tokenExpiradoDeveSerRejeitado() {
        ReflectionTestUtils.setField(jwtUtil, "accessExpirationMs", -1_000L);
        String tokenJaExpirado = jwtUtil.generateAccessToken("motorista@vanmos.com", "MOTORISTA", 1L);

        assertFalse(jwtUtil.isValidAccessToken(tokenJaExpirado));
    }

    @Test
    void tokenComTextoInvalidoDeveSerRejeitadoSemLancarExcecao() {
        assertFalse(jwtUtil.isValidAccessToken("isso-nao-e-um-jwt"));
    }

    @Test
    void tokenAssinadoComOutraChaveDeveSerRejeitado() {
        // Simula alguém tentando forjar um token sem conhecer o JWT_SECRET real.
        String token = jwtUtil.generateAccessToken("motorista@vanmos.com", "MOTORISTA", 1L);

        JwtUtil jwtUtilComOutraChave = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtilComOutraChave, "secret",
                "chave-diferente-0123456789abcdef0123456789abcdef0123456789ab");
        ReflectionTestUtils.setField(jwtUtilComOutraChave, "accessExpirationMs", 900_000L);

        assertFalse(jwtUtilComOutraChave.isValidAccessToken(token));
    }
}
