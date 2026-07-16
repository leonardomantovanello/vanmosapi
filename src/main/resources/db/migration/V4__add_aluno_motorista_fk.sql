-- Cada aluno passa a pertencer a um motorista específico (quem o cadastrou).
-- Antes, qualquer MOTORISTA autenticado enxergava/gerenciava TODOS os alunos
-- do sistema — só RESPONSAVEL já era filtrado pelo próprio (responsavel_id).
-- Nullable de propósito: linhas existentes (hoje nenhuma) não têm essa
-- associação ainda.
ALTER TABLE alunos ADD motorista_id BIGINT NULL;

ALTER TABLE alunos
    ADD CONSTRAINT FK_alunos_motorista
    FOREIGN KEY (motorista_id) REFERENCES passageiros(id);

CREATE INDEX IX_alunos_motorista_id ON alunos(motorista_id);
