-- Prepara a tabela `motorista` (hoje órfã, sem uso real) pra virar a fonte
-- de verdade das contas de motorista, tirando esse papel de `passageiros`.
-- Só schema aqui — a migração de dados de verdade é a V15, separada, pra
-- poder validar todo o código novo contra essa tabela ainda vazia antes de
-- mexer em qualquer linha real.
--
-- `ativo` NÃO entra na lista abaixo: já existe fisicamente na tabela desde
-- antes do Flyway (baseline-version=0) — Motorista.java já mapeava esse
-- campo, só nunca teve migração criando a tabela em si.
ALTER TABLE motorista ADD senha NVARCHAR(255) NULL;
ALTER TABLE motorista ADD idade INT NULL;
ALTER TABLE motorista ADD genero NVARCHAR(20) NULL;
ALTER TABLE motorista ADD aceito_termos BIT NOT NULL DEFAULT 0;
ALTER TABLE motorista ADD rg NVARCHAR(20) NULL;
ALTER TABLE motorista ADD rg_documento_base64 NVARCHAR(MAX) NULL;
ALTER TABLE motorista ADD cnh_documento_base64 NVARCHAR(MAX) NULL;
ALTER TABLE motorista ADD status_cadastro NVARCHAR(20) NULL;
ALTER TABLE motorista ADD motivo_reprovacao NVARCHAR(MAX) NULL;
ALTER TABLE motorista ADD criado_em DATETIME2 NULL;
ALTER TABLE motorista ADD avatar_base64 NVARCHAR(MAX) NULL;
-- Só existiam em motoristas_admin — necessários pra página pública "Nossos
-- Motoristas" continuar funcionando depois que essa tabela for apagada
-- (ver V15). Ficam NULL pros motoristas migrados; o motorista completa
-- depois editando o próprio perfil.
ALTER TABLE motorista ADD modelo_van NVARCHAR(100) NULL;
ALTER TABLE motorista ADD placa_van NVARCHAR(20) NULL;
GO

UPDATE motorista SET criado_em = SYSDATETIME() WHERE criado_em IS NULL;
GO

ALTER TABLE motorista ALTER COLUMN criado_em DATETIME2 NOT NULL;
GO
