-- Fluxo de aprovação de motorista por e-mail (substitui a ativação manual
-- pelo antigo painel de admin). status_cadastro só é operacional para
-- tipo=MOTORISTA; PASSAGEIRO nunca passa por esse fluxo.
ALTER TABLE passageiros ADD status_cadastro NVARCHAR(20) NULL;
ALTER TABLE passageiros ADD telefone NVARCHAR(20) NULL;
ALTER TABLE passageiros ADD rg NVARCHAR(20) NULL;
ALTER TABLE passageiros ADD cnh NVARCHAR(20) NULL;
ALTER TABLE passageiros ADD rg_documento_base64 NVARCHAR(MAX) NULL;
ALTER TABLE passageiros ADD cnh_documento_base64 NVARCHAR(MAX) NULL;
ALTER TABLE passageiros ADD motivo_reprovacao NVARCHAR(MAX) NULL;
ALTER TABLE passageiros ADD criado_em DATETIME2 NULL;
GO

-- Batch separado (GO) — SQL Server compila o batch inteiro antes de
-- executar, então as colunas adicionadas acima só ficam "visíveis" pras
-- instruções abaixo depois de um novo batch.

-- Backfill só para MOTORISTA: ativo=1 (já vetado no painel antigo) vira
-- APROVADO; ativo=0 é ambíguo (nunca revisado vs. desativado depois) e vira
-- PENDENTE — o lado seguro, pra suporte revisar de novo em vez de travar um
-- motorista legítimo pra sempre.
UPDATE passageiros SET status_cadastro = CASE WHEN ativo = 1 THEN 'APROVADO' ELSE 'PENDENTE' END
WHERE tipo = 'MOTORISTA';
-- PASSAGEIRO: status_cadastro fica NULL (não se aplica, fora deste fluxo).

UPDATE passageiros SET criado_em = SYSDATETIME() WHERE criado_em IS NULL;
GO

ALTER TABLE passageiros ALTER COLUMN criado_em DATETIME2 NOT NULL;
GO

-- Tokens de uso único do link de aprovação/reprovação por e-mail — mesmo
-- desenho de password_reset_tokens (V11), mas com validade maior (7 dias,
-- ver PassageiroService): revisão de documento por humano leva mais tempo
-- que redefinir senha.
CREATE TABLE cadastro_aprovacao_tokens (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    token NVARCHAR(100) NOT NULL,
    passageiro_id BIGINT NOT NULL,
    expira_em DATETIME2 NOT NULL,
    usado BIT NOT NULL DEFAULT 0,
    CONSTRAINT UQ_cadastro_aprovacao_tokens_token UNIQUE (token),
    CONSTRAINT FK_cadastro_aprovacao_tokens_passageiro FOREIGN KEY (passageiro_id) REFERENCES passageiros(id)
);

CREATE INDEX IX_cadastro_aprovacao_tokens_passageiro_id ON cadastro_aprovacao_tokens(passageiro_id);
