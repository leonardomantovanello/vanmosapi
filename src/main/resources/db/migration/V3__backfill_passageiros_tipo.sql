-- A coluna "tipo" já existia fisicamente (com CHECK 'MOTORISTA'/'PASSAGEIRO'),
-- mas nunca foi mapeada na entidade Java — toda linha ficava com tipo NULL e
-- o backend tratava qualquer cadastro como PASSAGEIRO (role RESPONSAVEL) na
-- hora do login. Os 5 registros de teste existentes hoje são, na prática,
-- cadastros feitos pela tela de motorista do site (ver Register.jsx do
-- projeto VanMos, que registra e redireciona para o dashboard /motorista) —
-- por isso o backfill assume MOTORISTA para tudo que está NULL agora.
UPDATE passageiros SET tipo = 'MOTORISTA' WHERE tipo IS NULL;

ALTER TABLE passageiros ALTER COLUMN tipo VARCHAR(20) NOT NULL;
