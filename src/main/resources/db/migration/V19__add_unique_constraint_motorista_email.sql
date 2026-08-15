-- motorista.cpf e motorista.cnh já são unique (V14), mas email nunca foi —
-- só era checado a nivel de aplicacao dentro da propria tabela, o que
-- permitia (e permitiu, na pratica) o mesmo email existir em `passageiros`
-- e em `motorista` ao mesmo tempo. O app agora tambem bloqueia isso na
-- criacao/edicao das duas contas (PassageiroService/MotoristaService); esta
-- constraint garante a mesma regra a nivel de banco, espelhando
-- UQ_passageiros_email.
ALTER TABLE motorista ADD CONSTRAINT UQ_motorista_email UNIQUE (email);
GO
