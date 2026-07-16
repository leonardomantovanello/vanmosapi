-- Chat motorista<->responsável, escopado por aluno. Antes vivia só em
-- memória no app (MOCK_MESSAGES) e se perdia ao fechar o app.
CREATE TABLE mensagens (
    id             BIGINT IDENTITY(1,1) PRIMARY KEY,
    aluno_id       BIGINT NOT NULL,
    remetente_tipo VARCHAR(20) NOT NULL,
    remetente_id   BIGINT NOT NULL,
    texto          VARCHAR(2000) NOT NULL,
    criado_em      DATETIME2 NOT NULL,
    CONSTRAINT FK_mensagens_aluno FOREIGN KEY (aluno_id) REFERENCES alunos(id)
);

CREATE INDEX IX_mensagens_aluno_id ON mensagens(aluno_id);
