-- Datos opcionales: funciones futuras y transacciones de ejemplo; no son cuentas de autenticación reales.
USE entrefunciones;
-- Usuarios: 1 administrador + 3 clientes.
INSERT INTO usuario (id, nombres, apellidos, correo, contrasena, dni, activo) VALUES
    (1, 'Marco',    'Reyes',    'marco.reyes@entrefunciones.pe',   'hash_admin1',   '70011122', TRUE),
    (2, 'Valeria',  'Suarez',   'valeria.suarez@gmail.com',        'hash_cliente1', '71122334', TRUE),
    (3, 'Diego',    'Palacios', 'diego.palacios@gmail.com',        'hash_cliente2', '72233445', TRUE),
    (4, 'Camila',   'Torres',   'camila.torres@gmail.com',         'hash_cliente3', '73344556', FALSE);

INSERT INTO administrador (usuario_id, codigo_empleado) VALUES
    (1, 'EMP-0001');

INSERT INTO cliente (usuario_id, fecha_registro, telefono) VALUES
    (2, '2026-01-15', '987654321'),
    (3, '2026-02-03', '987654322'),
    (4, '2025-11-20', '987654323');  -- cuenta desactivada (eliminado logico)

-- Sedes y salas.
INSERT INTO sede (id, razon_social, direccion, ciudad, telefono_contacto, activo) VALUES
    (1, 'EntreFunciones Miraflores S.A.C.', 'Av. Larco 1234', 'Lima', '014001122', TRUE),
    (2, 'EntreFunciones San Isidro S.A.C.', 'Av. Javier Prado 4321', 'Lima', '014003344', TRUE);

INSERT INTO sala (id, sede_id, nombre, aforo, tipo_sala, activo) VALUES
    (1, 1, 'Sala 1', 80, 'Cine 2D', TRUE),
    (2, 1, 'Sala 2', 40, 'Teatro',  TRUE),
    (3, 2, 'Sala 1', 60, 'Cine 3D', TRUE);

INSERT INTO butaca (id, sala_id, fila, columna, tipo_butaca, activo) VALUES
    (1, 1, 'A', 1, 'General', TRUE),
    (2, 1, 'A', 2, 'General', TRUE),
    (3, 1, 'A', 3, 'Preferencial', TRUE),
    (4, 2, 'A', 1, 'General', TRUE),
    (5, 2, 'A', 2, 'General', FALSE); -- dada de baja (p.ej. butaca dañada)

-- Butacas adicionales para completar el aforo declarado de cada sala
-- (la seccion original solo tenia unas pocas butacas de muestra).
INSERT INTO butaca (id, sala_id, fila, columna, tipo_butaca, activo) VALUES
    (6, 1, 'A', 4, 'General', TRUE),
    (7, 1, 'A', 5, 'General', TRUE),
    (8, 1, 'A', 6, 'General', TRUE),
    (9, 1, 'A', 7, 'General', TRUE),
    (10, 1, 'A', 8, 'General', TRUE),
    (11, 1, 'A', 9, 'General', TRUE),
    (12, 1, 'A', 10, 'General', TRUE),
    (13, 1, 'B', 1, 'General', TRUE),
    (14, 1, 'B', 2, 'General', TRUE),
    (15, 1, 'B', 3, 'General', TRUE),
    (16, 1, 'B', 4, 'General', TRUE),
    (17, 1, 'B', 5, 'General', TRUE),
    (18, 1, 'B', 6, 'General', TRUE),
    (19, 1, 'B', 7, 'General', TRUE),
    (20, 1, 'B', 8, 'General', TRUE),
    (21, 1, 'B', 9, 'General', TRUE),
    (22, 1, 'B', 10, 'General', TRUE),
    (23, 1, 'C', 1, 'General', TRUE),
    (24, 1, 'C', 2, 'General', TRUE),
    (25, 1, 'C', 3, 'General', TRUE),
    (26, 1, 'C', 4, 'General', TRUE),
    (27, 1, 'C', 5, 'General', TRUE),
    (28, 1, 'C', 6, 'General', TRUE),
    (29, 1, 'C', 7, 'General', TRUE),
    (30, 1, 'C', 8, 'General', TRUE),
    (31, 1, 'C', 9, 'General', TRUE),
    (32, 1, 'C', 10, 'General', TRUE),
    (33, 1, 'D', 1, 'General', TRUE),
    (34, 1, 'D', 2, 'General', TRUE),
    (35, 1, 'D', 3, 'General', TRUE),
    (36, 1, 'D', 4, 'General', TRUE),
    (37, 1, 'D', 5, 'General', TRUE),
    (38, 1, 'D', 6, 'General', TRUE),
    (39, 1, 'D', 7, 'General', TRUE),
    (40, 1, 'D', 8, 'General', TRUE),
    (41, 1, 'D', 9, 'General', TRUE),
    (42, 1, 'D', 10, 'General', TRUE),
    (43, 1, 'E', 1, 'General', TRUE),
    (44, 1, 'E', 2, 'General', TRUE),
    (45, 1, 'E', 3, 'General', TRUE),
    (46, 1, 'E', 4, 'General', TRUE),
    (47, 1, 'E', 5, 'General', TRUE),
    (48, 1, 'E', 6, 'General', TRUE),
    (49, 1, 'E', 7, 'General', TRUE),
    (50, 1, 'E', 8, 'General', TRUE),
    (51, 1, 'E', 9, 'General', TRUE),
    (52, 1, 'E', 10, 'General', TRUE),
    (53, 1, 'F', 1, 'General', TRUE),
    (54, 1, 'F', 2, 'General', TRUE),
    (55, 1, 'F', 3, 'General', TRUE),
    (56, 1, 'F', 4, 'General', TRUE),
    (57, 1, 'F', 5, 'General', TRUE),
    (58, 1, 'F', 6, 'General', TRUE),
    (59, 1, 'F', 7, 'General', TRUE),
    (60, 1, 'F', 8, 'General', TRUE),
    (61, 1, 'F', 9, 'General', TRUE),
    (62, 1, 'F', 10, 'General', TRUE),
    (63, 1, 'G', 1, 'Preferencial', TRUE),
    (64, 1, 'G', 2, 'Preferencial', TRUE),
    (65, 1, 'G', 3, 'Preferencial', TRUE),
    (66, 1, 'G', 4, 'Preferencial', TRUE),
    (67, 1, 'G', 5, 'Preferencial', TRUE),
    (68, 1, 'G', 6, 'Preferencial', TRUE),
    (69, 1, 'G', 7, 'Preferencial', TRUE),
    (70, 1, 'G', 8, 'Preferencial', TRUE),
    (71, 1, 'G', 9, 'Preferencial', TRUE),
    (72, 1, 'G', 10, 'Preferencial', TRUE),
    (73, 1, 'H', 1, 'Accesible', TRUE),
    (74, 1, 'H', 2, 'General', TRUE),
    (75, 1, 'H', 3, 'General', TRUE),
    (76, 1, 'H', 4, 'General', TRUE),
    (77, 1, 'H', 5, 'General', TRUE),
    (78, 1, 'H', 6, 'General', TRUE),
    (79, 1, 'H', 7, 'General', TRUE),
    (80, 1, 'H', 8, 'General', TRUE),
    (81, 1, 'H', 9, 'General', TRUE),
    (82, 1, 'H', 10, 'Accesible', TRUE),
    (83, 2, 'A', 3, 'General', TRUE),
    (84, 2, 'A', 4, 'General', TRUE),
    (85, 2, 'A', 5, 'General', TRUE),
    (86, 2, 'A', 6, 'General', TRUE),
    (87, 2, 'A', 7, 'General', TRUE),
    (88, 2, 'A', 8, 'General', TRUE),
    (89, 2, 'A', 9, 'General', TRUE),
    (90, 2, 'A', 10, 'General', TRUE),
    (91, 2, 'B', 1, 'Preferencial', TRUE),
    (92, 2, 'B', 2, 'Preferencial', TRUE),
    (93, 2, 'B', 3, 'Preferencial', TRUE),
    (94, 2, 'B', 4, 'Preferencial', TRUE),
    (95, 2, 'B', 5, 'Preferencial', TRUE),
    (96, 2, 'B', 6, 'Preferencial', TRUE),
    (97, 2, 'B', 7, 'Preferencial', TRUE),
    (98, 2, 'B', 8, 'Preferencial', TRUE),
    (99, 2, 'B', 9, 'Preferencial', TRUE),
    (100, 2, 'B', 10, 'Preferencial', TRUE),
    (101, 2, 'C', 1, 'Preferencial', TRUE),
    (102, 2, 'C', 2, 'Preferencial', TRUE),
    (103, 2, 'C', 3, 'Preferencial', TRUE),
    (104, 2, 'C', 4, 'Preferencial', TRUE),
    (105, 2, 'C', 5, 'Preferencial', TRUE),
    (106, 2, 'C', 6, 'Preferencial', TRUE),
    (107, 2, 'C', 7, 'Preferencial', TRUE),
    (108, 2, 'C', 8, 'Preferencial', TRUE),
    (109, 2, 'C', 9, 'Preferencial', TRUE),
    (110, 2, 'C', 10, 'Preferencial', TRUE),
    (111, 2, 'D', 1, 'Accesible', TRUE),
    (112, 2, 'D', 2, 'General', TRUE),
    (113, 2, 'D', 3, 'General', TRUE),
    (114, 2, 'D', 4, 'General', TRUE),
    (115, 2, 'D', 5, 'General', TRUE),
    (116, 2, 'D', 6, 'General', TRUE),
    (117, 2, 'D', 7, 'General', TRUE),
    (118, 2, 'D', 8, 'General', TRUE),
    (119, 2, 'D', 9, 'General', TRUE),
    (120, 2, 'D', 10, 'Accesible', TRUE),
    (121, 3, 'A', 1, 'General', TRUE),
    (122, 3, 'A', 2, 'General', TRUE),
    (123, 3, 'A', 3, 'General', TRUE),
    (124, 3, 'A', 4, 'General', TRUE),
    (125, 3, 'A', 5, 'General', TRUE),
    (126, 3, 'A', 6, 'General', TRUE),
    (127, 3, 'A', 7, 'General', TRUE),
    (128, 3, 'A', 8, 'General', TRUE),
    (129, 3, 'A', 9, 'General', TRUE),
    (130, 3, 'A', 10, 'General', TRUE),
    (131, 3, 'B', 1, 'Preferencial', TRUE),
    (132, 3, 'B', 2, 'Preferencial', TRUE),
    (133, 3, 'B', 3, 'Preferencial', TRUE),
    (134, 3, 'B', 4, 'Preferencial', TRUE),
    (135, 3, 'B', 5, 'Preferencial', TRUE),
    (136, 3, 'B', 6, 'Preferencial', TRUE),
    (137, 3, 'B', 7, 'Preferencial', TRUE),
    (138, 3, 'B', 8, 'Preferencial', TRUE),
    (139, 3, 'B', 9, 'Preferencial', TRUE),
    (140, 3, 'B', 10, 'Preferencial', TRUE),
    (141, 3, 'C', 1, 'Preferencial', TRUE),
    (142, 3, 'C', 2, 'Preferencial', TRUE),
    (143, 3, 'C', 3, 'Preferencial', TRUE),
    (144, 3, 'C', 4, 'Preferencial', TRUE),
    (145, 3, 'C', 5, 'Preferencial', TRUE),
    (146, 3, 'C', 6, 'Preferencial', TRUE),
    (147, 3, 'C', 7, 'Preferencial', TRUE),
    (148, 3, 'C', 8, 'Preferencial', TRUE),
    (149, 3, 'C', 9, 'Preferencial', TRUE),
    (150, 3, 'C', 10, 'Preferencial', TRUE),
    (151, 3, 'D', 1, 'Preferencial', TRUE),
    (152, 3, 'D', 2, 'Preferencial', TRUE),
    (153, 3, 'D', 3, 'Preferencial', TRUE),
    (154, 3, 'D', 4, 'Preferencial', TRUE),
    (155, 3, 'D', 5, 'Preferencial', TRUE),
    (156, 3, 'D', 6, 'Preferencial', TRUE),
    (157, 3, 'D', 7, 'Preferencial', TRUE),
    (158, 3, 'D', 8, 'Preferencial', TRUE),
    (159, 3, 'D', 9, 'Preferencial', TRUE),
    (160, 3, 'D', 10, 'Preferencial', TRUE),
    (161, 3, 'E', 1, 'General', TRUE),
    (162, 3, 'E', 2, 'General', TRUE),
    (163, 3, 'E', 3, 'General', TRUE),
    (164, 3, 'E', 4, 'General', TRUE),
    (165, 3, 'E', 5, 'General', TRUE),
    (166, 3, 'E', 6, 'General', TRUE),
    (167, 3, 'E', 7, 'General', TRUE),
    (168, 3, 'E', 8, 'General', TRUE),
    (169, 3, 'E', 9, 'General', TRUE),
    (170, 3, 'E', 10, 'General', TRUE),
    (171, 3, 'F', 1, 'Accesible', TRUE),
    (172, 3, 'F', 2, 'General', TRUE),
    (173, 3, 'F', 3, 'General', TRUE),
    (174, 3, 'F', 4, 'General', TRUE),
    (175, 3, 'F', 5, 'General', TRUE),
    (176, 3, 'F', 6, 'General', TRUE),
    (177, 3, 'F', 7, 'General', TRUE),
    (178, 3, 'F', 8, 'General', TRUE),
    (179, 3, 'F', 9, 'General', TRUE),
    (180, 3, 'F', 10, 'Accesible', TRUE);



-- Catalogo: 5 eventos. Interestelar (evento 1) tiene DOS funciones (1 y 3,
-- horarios distintos) que comparten el mismo evento_id: el catalogo
-- (titulo, genero, director, precio) vive una sola vez.
INSERT INTO evento (id, precio, activo, imagen_url) VALUES
    (1, 25.00, TRUE, 'img/eventos/interestelar.jpg'),        -- Interestelar
    (2, 22.00, TRUE, 'img/eventos/casa-del-terror.jpg'),     -- La Casa del Terror
    (3, 35.00, TRUE, 'img/eventos/la-muralla.jpg'),          -- La Muralla
    (4, 10.00, TRUE, 'img/eventos/charla-cine-peruano.jpg'), -- Charla de Cine Peruano Contemporaneo
    (5, 15.00, TRUE, 'img/eventos/taller-foto.jpg');         -- Taller de Fotografia

INSERT INTO pelicula (evento_id, nombre_pelicula, duracion_minutos, genero, clasificacion_edad, director) VALUES
    (1, 'Interestelar', 169, 'DRAMA', 'PG13', 'Christopher Nolan'),
    (2, 'La Casa del Terror', 95, 'TERROR', 'MAYORES_18', 'Ana Ramirez');

INSERT INTO obra_teatro (evento_id, titulo, nombre_director, elenco) VALUES
    (3, 'La Muralla', 'Sofia Herrera', 'Juan Perez, Maria Lopez');

INSERT INTO actividad_cultural (evento_id, nombre, expositor, tipo_actividad) VALUES
    (4, 'Charla de Cine Peruano Contemporaneo', 'Rodrigo Vega', 'CHARLA'),
    (5, 'Taller de Fotografia', 'Rodrigo Vega', 'TALLER');

-- Funciones: 6 en total (una por horario). Las funciones 1 y 3 programan
-- el mismo evento (Interestelar) en dos horarios distintos. La funcion 6
-- programa una actividad cultural (evento 5): vende por cupo, no por
-- butaca (ver reserva 4 mas abajo).
INSERT INTO funcion (id, evento_id, sala_id, fecha, hora_inicio, hora_fin, idioma, estado) VALUES
    (1, 1, 1, (CURRENT_DATE + INTERVAL 2 DAY), '15:00:00', '17:49:00', 'Español',     'CONFIRMADA'),
    (2, 2, 1, (CURRENT_DATE + INTERVAL 2 DAY), '21:00:00', '22:35:00', 'Subtitulada', 'PROGRAMADA'),
    (3, 1, 1, (CURRENT_DATE + INTERVAL 2 DAY), '18:00:00', '20:49:00', 'Subtitulada', 'CONFIRMADA'),
    (4, 3, 2, (CURRENT_DATE + INTERVAL 3 DAY), '19:00:00', '21:00:00', 'Español',     'PROGRAMADA'),
    (5, 4, 3, (CURRENT_DATE + INTERVAL 1 DAY), '17:00:00', '18:30:00', 'Español',     'CANCELADA'),
    (6, 5, 2, (CURRENT_DATE + INTERVAL 4 DAY), '18:00:00', '19:00:00', 'Español',     'CONFIRMADA');

-- Reservas: una confirmada (flujo completo), una pendiente de pago, una
-- expirada, y una de aforo general (funcion 6, actividad cultural: cantidad
-- en vez de butacas, sin filas en reserva_butaca).
INSERT INTO reserva (id, funcion_id, cliente_id, cantidad, precio_unitario, fecha_creacion, fecha_expiracion, estado) VALUES
    (1, 1, 2, NULL, 25.00, '2026-09-14 10:00:00', '2026-09-14 10:15:00', 'CONFIRMADA'),
    (2, 3, 3, NULL, 25.00, '2026-09-14 11:00:00', '2026-09-14 11:15:00', 'ACTIVA'),
    (3, 2, 2, NULL, 22.00, '2026-09-10 09:00:00', '2026-09-10 09:15:00', 'EXPIRADA'),
    (4, 6, 3, 2, 15.00, '2026-09-16 10:00:00', '2026-09-16 10:15:00', 'CONFIRMADA');

INSERT INTO reserva_butaca (reserva_id, butaca_id) VALUES
    (1, 1), (1, 2),   -- Valeria reservo A1 y A2 para la funcion 1
    (2, 3),           -- Diego reservo A3 (Preferencial) para la funcion 3
    (3, 1);           -- reserva vencida de Valeria para la funcion 2 (butaca A1, otra funcion: sin conflicto)
                      -- la reserva 4 (aforo general) no tiene filas aqui: usa reserva.cantidad.

-- Pagos: uno validado, uno pendiente, uno rechazado (para la reserva vencida), uno validado (aforo general).
INSERT INTO pago (id, reserva_id, codigo_pago, monto, fecha_pago, estado) VALUES
    (1, 1, 'PAY-0001', 50.00, '2026-09-14 10:05:00', 'VALIDADO'),
    (2, 2, 'PAY-0002', 25.00, NULL, 'PENDIENTE'),
    (3, 3, 'PAY-0003', 25.00, '2026-09-10 09:10:00', 'RECHAZADO'),
    (4, 4, 'PAY-0004', 30.00, '2026-09-16 10:05:00', 'VALIDADO');

-- Entradas: se emiten para las reservas ya confirmadas/pagadas (reserva 1 y
-- reserva 4). Las de la reserva 4 son de aforo general: butaca_id queda NULL.
INSERT INTO entrada (id, reserva_id, butaca_id, fecha_emision, codigo_qr, precio_final, estado) VALUES
    (1, 1, 1,    '2026-09-14 10:05:30', 'QR-0000000001', 25.00, 'VIGENTE'),
    (2, 1, 2,    '2026-09-14 10:05:30', 'QR-0000000002', 25.00, 'VIGENTE'),
    (3, 4, NULL, '2026-09-16 10:05:30', 'QR-0000000003', 15.00, 'VIGENTE'),
    (4, 4, NULL, '2026-09-16 10:05:30', 'QR-0000000004', 15.00, 'VIGENTE');

-- Auditoria: una incidencia ligada a una funcion (RF-M6-04) y un registro
-- de trazabilidad general sin funcion asociada (RNF-SEG-01).
INSERT INTO auditoria (id, funcion_id, fecha, responsable_id, responsable, descripcion, estado) VALUES
    (1, 1, '2026-09-14 10:06:00', 1, NULL, 'Cliente reporto butaca A2 con reposabrazos suelto durante la funcion.', 'PENDIENTE'),
    -- Caso legado deliberado: el texto identifica a una cliente, no a un
    -- administrador. Se conserva para resolucion manual y no se fuerza una FK.
    (2, NULL, '2026-09-14 09:58:00', NULL, 'valeria.suarez@gmail.com', 'Inicio de sesion exitoso desde nueva ubicacion.', 'RESUELTA');
