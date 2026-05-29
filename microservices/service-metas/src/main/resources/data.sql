-- Seeding Metas using H2 MERGE
-- Columns: id, kpi_id, equipo_id, valor_objetivo, fecha_limite, estado
REPLACE INTO metas (id, kpi_id, equipo_id, valor_objetivo, fecha_limite, estado) VALUES 
(1, 1, 1, 20000000.0, '2026-06-30', 'ACTIVA'), -- Goal: 20M CLP Sales for Ventas Norte
(2, 2, 1, 3.0, '2026-06-30', 'ACTIVA'),        -- Goal: 3% Conversion for Ventas Norte
(3, 1, 2, 18000000.0, '2026-06-30', 'ACTIVA'), -- Goal: 18M CLP Sales for Ventas Sur
(4, 3, 4, 50000.0, '2026-06-30', 'ACTIVA'),    -- Goal: Maintain 50k stock in Bodega Central
(5, 4, 5, 24.0, '2026-06-30', 'ACTIVA'),       -- Goal: Under 24 hours dispatch for Distribución
(6, 6, 6, 90.0, '2026-06-30', 'CUMPLIDA');     -- Goal: 90% training completion for Capacitación
