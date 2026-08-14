-- Migração de dados real: move todo mundo com tipo='MOTORISTA' de
-- `passageiros` pra `motorista` (schema já preparado na V14), repontando as
-- FKs que hoje apontam pra `passageiros(id)`. Ponto de não-retorno — só
-- roda depois de backup + validação do código novo (ver V14/V15 e o plano
-- de migração).
--
-- Preserva o `id` exato de cada motorista (SET IDENTITY_INSERT). Como
-- `rota_paradas.motorista_id`, `rota_progresso.motorista_id` (PK) e
-- `mensagens.remetente_id` não têm FK física pra `passageiros`, preservar o
-- id significa que essas três tabelas não precisam de nenhuma alteração de
-- dado — o histórico de rota/mensagens continua íntegro sem tocar nelas.

-- 1) Backup de segurança da tabela inteira antes de qualquer DELETE.
SELECT * INTO passageiros_backup_pre_migracao FROM passageiros;
GO

-- 1.1) Descarta cadastros de teste explicitamente marcados como
-- descartáveis pelo próprio autor (nome "PODE DELETAR"), nunca aprovados
-- de verdade (um PENDENTE, um REPROVADO) — ficaram com a mesma CNH de
-- teste ('12345678900'), o que quebraria a unique constraint de
-- motorista.cnh no passo seguinte. Remove primeiro os tokens que
-- referenciam essas duas linhas (FK ainda aponta pra passageiros nesse
-- ponto — a repontagem só acontece no passo 5), senão o DELETE abaixo falha.
DELETE FROM cadastro_aprovacao_tokens WHERE passageiro_id IN (75, 76);
DELETE FROM password_reset_tokens WHERE passageiro_id IN (75, 76);
DELETE FROM passageiros WHERE id IN (75, 76) AND tipo = 'MOTORISTA';
GO

-- 2) Copia os motoristas pra tabela nova, preservando o id. cpf/cnh vazios
-- ('', não NULL) viram NULL — string vazia repetida quebra a unique
-- constraint, NULL não (várias linhas sem CNH cadastrada é o caso real
-- aqui, não um valor "igual" de verdade).
SET IDENTITY_INSERT motorista ON;

INSERT INTO motorista (
    id, nome, cpf, cnh, idade, genero, telefone, email, ativo,
    senha, aceito_termos, rg, rg_documento_base64, cnh_documento_base64,
    status_cadastro, motivo_reprovacao, criado_em, avatar_base64
)
SELECT
    id, nome, NULLIF(cpf, ''), NULLIF(cnh, ''), idade, genero, telefone, email, ativo,
    senha, aceitou_termos, rg, rg_documento_base64, cnh_documento_base64,
    status_cadastro, motivo_reprovacao, criado_em, avatar_base64
FROM passageiros
WHERE tipo = 'MOTORISTA';

SET IDENTITY_INSERT motorista OFF;
GO

-- 3) Reajusta o contador de identity pro maior id copiado, pra próximos
-- cadastros novos de motorista não colidirem com os ids preservados.
DECLARE @maxId BIGINT = (SELECT MAX(id) FROM motorista);
IF @maxId IS NOT NULL
    DBCC CHECKIDENT ('motorista', RESEED, @maxId);
GO

-- 4) Repontar FK de alunos.motorista_id pra motorista(id) em vez de
-- passageiros(id) (ver V4).
ALTER TABLE alunos DROP CONSTRAINT FK_alunos_motorista;
GO

ALTER TABLE alunos
    ADD CONSTRAINT FK_alunos_motorista
    FOREIGN KEY (motorista_id) REFERENCES motorista(id);
GO

-- 5) cadastro_aprovacao_tokens é exclusivo de motorista (só motorista gera
-- esse token) — repontar a FK e renomear a coluna pra motorista_id, já que
-- CadastroAprovacaoToken.java já mapeia esse nome (ver V12).
ALTER TABLE cadastro_aprovacao_tokens DROP CONSTRAINT FK_cadastro_aprovacao_tokens_passageiro;
GO

EXEC sp_rename 'cadastro_aprovacao_tokens.passageiro_id', 'motorista_id', 'COLUMN';
GO

ALTER TABLE cadastro_aprovacao_tokens
    ADD CONSTRAINT FK_cadastro_aprovacao_tokens_motorista
    FOREIGN KEY (motorista_id) REFERENCES motorista(id);
GO

-- 6) password_reset_tokens era compartilhado entre passageiro e motorista;
-- motorista passa a ter sua própria tabela (motorista_password_reset_tokens,
-- ver V15). Qualquer link de redefinição pendente de um motorista nesse
-- momento simplesmente expira — ele pede um novo, agora pelo endpoint certo.
DELETE FROM password_reset_tokens
WHERE passageiro_id IN (SELECT id FROM passageiros WHERE tipo = 'MOTORISTA');
GO

-- 7) Agora seguro remover os motoristas de passageiros (sem FK pendente).
DELETE FROM passageiros WHERE tipo = 'MOTORISTA';
GO

-- 8) motoristas_admin confirmada órfã (sem uso real, sem ninguém
-- referenciando) — apagada de vez.
DROP TABLE motoristas_admin;
GO
