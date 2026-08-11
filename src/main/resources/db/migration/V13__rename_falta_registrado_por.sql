-- O sistema de faltas foi invertido: agora o RESPONSAVEL registra a falta
-- (antes só o motorista podia). Essa coluna guardava o id do motorista que
-- registrou; renomeada pra refletir que agora guarda o id de quem registrou
-- (responsavel, ou admin em casos excepcionais), não mais especificamente
-- um motorista.
EXEC sp_rename 'faltas.registrado_por_motorista_id', 'registrado_por_id', 'COLUMN';
