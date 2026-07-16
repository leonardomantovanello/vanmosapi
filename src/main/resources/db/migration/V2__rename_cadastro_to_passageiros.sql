-- A tabela "cadastro" sempre guardou quem loga como RESPONSAVEL (passageiro/
-- responsável), nunca motoristas — mas o nome ambíguo levava a confundir os
-- dois. Renomeada para "passageiros" para casar com a entidade Passageiro e
-- deixar explícito que ela é distinta de motorista/motoristas_admin.
--
-- sp_rename só troca o nome do objeto: FKs e índices que apontam para esta
-- tabela (ex: FK_alunos_responsavel em alunos.responsavel_id) continuam
-- funcionando sem qualquer alteração, pois referenciam o object_id interno,
-- não o nome. Só renomeamos os nomes das constraints abaixo por clareza.
EXEC sp_rename 'cadastro', 'passageiros';

EXEC sp_rename 'passageiros.UQ_cadastro_email', 'UQ_passageiros_email', 'INDEX';
EXEC sp_rename 'passageiros.UQ_cadastro_cpf', 'UQ_passageiros_cpf', 'INDEX';
EXEC sp_rename 'FK_alunos_responsavel', 'FK_alunos_passageiro', 'OBJECT';
