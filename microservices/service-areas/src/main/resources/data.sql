-- Seeding Areas
REPLACE INTO areas (id, nombre, descripcion) VALUES 
(1, 'Ventas y Comercial', 'Área encargada de comercializar los productos y generar ingresos.'),
(2, 'Operaciones y Logística', 'Área de distribución, control de inventario y despacho central.'),
(3, 'Recursos Humanos', 'Gestión de talento, clima laboral y capacitación.'),
(4, 'Finanzas', 'Control financiero, presupuestos y auditorías.');

-- Seeding Equipos
REPLACE INTO equipos (id, nombre, area_id, lider_id) VALUES 
(1, 'Ventas Norte', 1, 'jefe.ventas'),
(2, 'Ventas Sur', 1, 'jefe.ventas.sur'),
(3, 'E-Commerce', 1, 'jefe.ecommerce'),
(4, 'Bodega Central', 2, 'jefe.bodega'),
(5, 'Distribución y Transporte', 2, 'jefe.transporte'),
(6, 'Capacitación y Desarrollo', 3, 'jefe.capacitacion'),
(7, 'Contabilidad y Presupuesto', 4, 'jefe.contabilidad');

-- Seeding Usuarios
REPLACE INTO usuarios (username, password, nombre_completo, role, area_id, equipo_id) VALUES 
('admin', 'admin123', 'Administrador del Sistema', 'ADMIN', NULL, NULL),
('gerente', 'gerente123', 'Gerente Comercial', 'GERENTE', NULL, NULL),
('jefe.ventas', 'jefe123', 'Juan Pablo Rivera - Jefe de Ventas', 'JEFE_AREA', 1, NULL),
('jefe.bodega', 'jefe123', 'Carlos Gómez - Jefe de Bodega', 'JEFE_AREA', 2, NULL),
('jefe.transporte', 'jefe123', 'Sofía Rojas - Jefe de Transporte', 'JEFE_AREA', 2, NULL),
('jefe.capacitacion', 'jefe123', 'Marta Soto - Jefe de RRHH', 'JEFE_AREA', 3, NULL),
('jefe.contabilidad', 'jefe123', 'Ricardo Díaz - Jefe de Finanzas', 'JEFE_AREA', 4, NULL),
('juan.ventas', 'colab123', 'Nayaret Rivas - Colaboradora Ventas', 'COLABORADOR', 1, 1),
('pedro.bodega', 'colab123', 'Pedro Altamirano - Operario Bodega', 'COLABORADOR', 2, 4),
('ana.rrhh', 'colab123', 'Ana María Silva - Asistente RRHH', 'COLABORADOR', 3, 6);
