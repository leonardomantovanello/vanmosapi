-- Valor mensal cobrado pelo motorista por esse aluno (mensalidade do
-- transporte) — soma dos alunos ativos vira a "Receita Mensal" no dashboard
-- do motorista (ver Motorista.jsx). NULL é um estado válido: alunos
-- cadastrados antes dessa coluna existir, ou o motorista deixou em branco.
ALTER TABLE alunos ADD valor DECIMAL(10, 2) NULL;
