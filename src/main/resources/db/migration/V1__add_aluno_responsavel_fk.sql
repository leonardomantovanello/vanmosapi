-- Primeira FK real do schema: liga alunos ao cadastro (guardião) que os cadastrou.
-- Nullable de propósito: linhas existentes não têm essa associação ainda e
-- precisam ser preenchidas manualmente/por um script de backfill à parte —
-- esta migration só adiciona a estrutura, não infere associações.
ALTER TABLE alunos ADD responsavel_id BIGINT NULL;

ALTER TABLE alunos
    ADD CONSTRAINT FK_alunos_responsavel
    FOREIGN KEY (responsavel_id) REFERENCES cadastro(id);

CREATE INDEX IX_alunos_responsavel_id ON alunos(responsavel_id);
