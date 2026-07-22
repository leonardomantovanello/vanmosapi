-- Parada "atual" da rota de cada motorista, enquanto uma corrida está em
-- andamento. Sem GPS: o motorista avança manualmente parada por parada (ver
-- RotaController /progresso/*), e o responsável só enxerga a posição do
-- próprio filho relativa a essa parada atual (ver RotaParadaService).
CREATE TABLE rota_progresso (
    motorista_id    BIGINT PRIMARY KEY,
    parada_atual_id BIGINT NULL,
    atualizado_em   DATETIME2 NOT NULL,
    CONSTRAINT FK_rota_progresso_parada FOREIGN KEY (parada_atual_id) REFERENCES rota_paradas(id)
);
