-- Tokens de uso único para o fluxo "esqueci minha senha" via link por
-- e-mail. Cada pedido gera uma linha nova; o token expira em 30 minutos
-- (ver PassageiroService) e fica marcado como usado após a redefinição,
-- pra não poder ser reaproveitado se o link vazar/for reaberto.
CREATE TABLE password_reset_tokens (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    token NVARCHAR(100) NOT NULL,
    passageiro_id BIGINT NOT NULL,
    expira_em DATETIME2 NOT NULL,
    usado BIT NOT NULL DEFAULT 0,
    CONSTRAINT UQ_password_reset_tokens_token UNIQUE (token),
    CONSTRAINT FK_password_reset_tokens_passageiro FOREIGN KEY (passageiro_id) REFERENCES passageiros(id)
);

CREATE INDEX IX_password_reset_tokens_passageiro_id ON password_reset_tokens(passageiro_id);
