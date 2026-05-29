-- Seeding Areas
MERGE INTO areas (id, nombre, descripcion) KEY(id) VALUES 
(1, 'Ventas y Comercial', 'Área encargada de comercializar los productos y generar ingresos.'),
(2, 'Operaciones y Logística', 'Área de distribución, control de inventario y despacho central.'),
(3, 'Recursos Humanos', 'Gestión de talento, clima laboral y capacitación.'),
(4, 'Finanzas', 'Control financiero, presupuestos y auditorías.');

-- Seeding Equipos
MERGE INTO equipos (id, nombre, area_id, lider_id) KEY(id) VALUES 
(1, 'Ventas Norte', 1, 'jefe.ventas'),
(2, 'Ventas Sur', 1, 'jefe.ventas.sur'),
(3, 'E-Commerce', 1, 'jefe.ecommerce'),
(4, 'Bodega Central', 2, 'jefe.bodega'),
(5, 'Distribución y Transporte', 2, 'jefe.transporte'),
(6, 'Capacitación y Desarrollo', 3, 'jefe.capacitacion'),
(7, 'Contabilidad y Presupuesto', 4, 'jefe.contabilidad');
