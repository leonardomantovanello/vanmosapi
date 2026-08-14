-- Tokens de "esqueci minha senha" pra Motorista — mesmo desenho de
-- password_reset_tokens (V11), tabela própria porque motorista deixa de
-- compartilhar id space com passageiros a partir da V15.
CREATE TABLE motorista_password_reset_tokens (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    token NVARCHAR(100) NOT NULL,
    motorista_id BIGINT NOT NULL,
    expira_em DATETIME2 NOT NULL,
    usado BIT NOT NULL DEFAULT 0,
    CONSTRAINT UQ_motorista_password_reset_tokens_token UNIQUE (token),
    CONSTRAINT FK_motorista_password_reset_tokens_motorista FOREIGN KEY (motorista_id) REFERENCES motorista(id)
);

CREATE INDEX IX_motorista_password_reset_tokens_motorista_id ON motorista_password_reset_tokens(motorista_id);
