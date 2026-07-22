-- Foto de perfil (base64) — sem storage externo configurado no projeto, a
-- imagem (já comprimida no app antes do upload) vai direto no banco. NULL
-- é um estado válido e significa "sem foto", não "sem alteração" — o app
-- sempre reenvia o valor atual inteiro a cada save (ver profileService.ts).
ALTER TABLE passageiros ADD avatar_base64 NVARCHAR(MAX) NULL;
ALTER TABLE motoristas_admin ADD avatar_base64 NVARCHAR(MAX) NULL;
