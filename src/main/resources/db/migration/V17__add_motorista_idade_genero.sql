-- V14 rodou em produção antes de idade/genero terem sido adicionados ao seu
-- conteúdo (só percebido depois, ao notar que Register.jsx manda esses dois
-- campos no cadastro de motorista) — essa migração fecha essa lacuna
-- separadamente, sem reescrever a V14 já aplicada.
ALTER TABLE motorista ADD idade INT NULL;
ALTER TABLE motorista ADD genero NVARCHAR(20) NULL;
