-- Rota padronizada de um motorista: lista ordenada dos alunos que ele
-- busca/leva todos os dias. Não duplica endereço aqui — só referencia o
-- aluno (que já tem enderecoEmbarque/enderecoDesembarque em alunos); esta
-- tabela guarda apenas a ordem de paradas dentro da rota de cada motorista.
CREATE TABLE rota_paradas (
    id           BIGINT IDENTITY(1,1) PRIMARY KEY,
    motorista_id BIGINT NOT NULL,
    aluno_id     BIGINT NOT NULL,
    ordem        INT NOT NULL,
    criado_em    DATETIME2 NOT NULL,
    CONSTRAINT FK_rota_paradas_aluno FOREIGN KEY (aluno_id) REFERENCES alunos(id),
    CONSTRAINT UQ_rota_paradas_motorista_aluno UNIQUE (motorista_id, aluno_id)
);

CREATE INDEX IX_rota_paradas_motorista_id ON rota_paradas(motorista_id);
