-- Controle de faltas por aluno. Um dia sem registro aqui é considerado
-- normal (presente) — só existe registro para dias marcados como falta,
-- sempre com justificativa, e só o motorista do aluno pode criar/remover
-- (ver FaltaController).
CREATE TABLE faltas (
    id                          BIGINT IDENTITY(1,1) PRIMARY KEY,
    aluno_id                    BIGINT NOT NULL,
    data                        DATE NOT NULL,
    justificativa               VARCHAR(500) NULL,
    registrado_por_motorista_id BIGINT NOT NULL,
    criado_em                   DATETIME2 NOT NULL,
    CONSTRAINT FK_faltas_aluno FOREIGN KEY (aluno_id) REFERENCES alunos(id),
    CONSTRAINT UQ_faltas_aluno_data UNIQUE (aluno_id, data)
);

CREATE INDEX IX_faltas_aluno_id ON faltas(aluno_id);
