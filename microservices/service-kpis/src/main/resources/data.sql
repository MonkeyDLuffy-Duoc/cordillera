-- Seeding KPIs using H2 MERGE
-- Columns: id, tipo_kpi, nombre, descripcion, unidad_medida
REPLACE INTO kpis (id, tipo_kpi, nombre, descripcion, unidad_medida) VALUES 
(1, 'FINANCIERO', 'Ventas Totales Mensuales', 'Ingresos totales por ventas en retail físico y e-commerce de Grupo Cordillera.', 'CLP'),
(2, 'CUMPLIMIENTO', 'Tasa de Conversión de Ventas', 'Porcentaje de visitas en tienda que se convierten en compras efectivas.', '%'),
(3, 'OPERACIONAL', 'Stock de Bodega Central', 'Nivel de existencias físicas disponibles en la bodega principal.', 'Unidades'),
(4, 'OPERACIONAL', 'Tiempo Promedio de Despacho', 'Tiempo total promedio transcurrido desde la compra hasta la entrega al cliente.', 'Horas'),
(5, 'CUMPLIMIENTO', 'Satisfacción Clima Laboral', 'Porcentaje general de satisfacción interna según encuesta trimestral de RRHH.', '%'),
(6, 'CUMPLIMIENTO', 'Cumplimiento de Capacitaciones', 'Porcentaje de colaboradores que han completado sus capacitaciones mensuales asignadas.', '%');

-- Seeding Mediciones (Generous historical data for Q1 and Q2 2026 to render rich charts)
-- Columns: id, kpi_id, valor, fecha_registro, registrado_por
REPLACE INTO mediciones (id, kpi_id, valor, fecha_registro, registrado_por) VALUES 
-- KPI 1: Ventas Totales Mensuales (Financiero)
(1, 1, 15200000.0, '2026-01-15', 'admin'),
(2, 1, 16800000.0, '2026-02-15', 'jefe.ventas'),
(3, 1, 18500000.0, '2026-03-15', 'jefe.ventas'),
(4, 1, 17200000.0, '2026-04-15', 'jefe.ventas'),
(5, 1, 19800000.0, '2026-05-15', 'jefe.ventas'),
(6, 1, 21500000.0, '2026-06-15', 'jefe.ventas'),

-- KPI 2: Tasa de Conversión de Ventas (Cumplimiento)
(7, 2, 2.1, '2026-01-15', 'admin'),
(8, 2, 2.4, '2026-02-15', 'jefe.ventas'),
(9, 2, 2.8, '2026-03-15', 'jefe.ventas'),
(10, 2, 2.5, '2026-04-15', 'jefe.ventas'),
(11, 2, 3.1, '2026-05-15', 'jefe.ventas'),
(12, 2, 3.5, '2026-06-15', 'jefe.ventas'),

-- KPI 3: Stock de Bodega Central (Operacional)
(13, 3, 45000.0, '2026-01-15', 'admin'),
(14, 3, 48200.0, '2026-02-15', 'jefe.bodega'),
(15, 3, 52100.0, '2026-03-15', 'jefe.bodega'),
(16, 3, 41500.0, '2026-04-15', 'jefe.bodega'),
(17, 3, 49800.0, '2026-05-15', 'jefe.bodega'),
(18, 3, 55000.0, '2026-06-15', 'jefe.bodega'),

-- KPI 4: Tiempo Promedio de Despacho (Operacional)
(19, 4, 28.5, '2026-01-15', 'admin'),
(20, 4, 26.2, '2026-02-15', 'jefe.transporte'),
(21, 4, 24.8, '2026-03-15', 'jefe.transporte'),
(22, 4, 25.1, '2026-04-15', 'jefe.transporte'),
(23, 4, 22.0, '2026-05-15', 'jefe.transporte'),
(24, 4, 19.5, '2026-06-15', 'jefe.transporte'),

-- KPI 5: Satisfacción Clima Laboral (Cumplimiento - Trimestral)
(25, 5, 75.0, '2026-01-15', 'admin'),
(26, 5, 78.0, '2026-03-15', 'jefe.capacitacion'),
(27, 5, 82.0, '2026-06-15', 'jefe.capacitacion'),

-- KPI 6: Cumplimiento de Capacitaciones (Cumplimiento)
(28, 6, 60.0, '2026-01-15', 'admin'),
(29, 6, 65.0, '2026-02-15', 'jefe.capacitacion'),
(30, 6, 72.0, '2026-03-15', 'jefe.capacitacion'),
(31, 6, 80.0, '2026-04-15', 'jefe.capacitacion'),
(32, 6, 88.0, '2026-05-15', 'jefe.capacitacion'),
(33, 6, 95.0, '2026-06-15', 'jefe.capacitacion');
