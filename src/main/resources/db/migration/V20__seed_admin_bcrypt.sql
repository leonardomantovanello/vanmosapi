-- Conta de administrador para o painel do site (rota /admin-login → /admin-panel).
-- O antigo painel de admin foi removido na V12 e a tabela login_admin ficou sem
-- nenhuma linha utilizável com o novo LoginAdminController, que compara a senha
-- com BCrypt (passwordEncoder.matches). Esta migration garante ao menos um
-- administrador válido, com hash BCrypt (strength 12, compatível com o
-- BCryptPasswordEncoder(12) do SecurityConfig).
--
-- Credenciais semeadas:
--   e-mail: admin@vanmos.com
--   senha:  VanMos@2026
-- Troque a senha em produção gerando um novo hash BCrypt e rodando um UPDATE.

IF NOT EXISTS (SELECT 1 FROM login_admin WHERE email_ou_cpf = 'admin@vanmos.com')
    INSERT INTO login_admin (email_ou_cpf, senha, lembrar_me)
    VALUES ('admin@vanmos.com', '$2b$12$k19gGCW4Vc2bD3ZbkTA1Geo.qZthqRWFyJMYnD72bSryN/GcmFymi', 0);
GO

-- Se a linha já existia com senha em texto puro (esquema legado, pré-BCrypt),
-- reescreve com o hash — sem isso o login continuava falhando em matches().
UPDATE login_admin
SET senha = '$2b$12$k19gGCW4Vc2bD3ZbkTA1Geo.qZthqRWFyJMYnD72bSryN/GcmFymi'
WHERE email_ou_cpf = 'admin@vanmos.com';
GO
