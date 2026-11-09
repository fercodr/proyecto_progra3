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
    (3, 1, 'A', 3, 'VIP',     TRUE),
    (4, 2, 'A', 1, 'General', TRUE),
    (5, 2, 'A', 2, 'General', FALSE); -- dada de baja (p.ej. butaca dañada)



-- Catalogo: 5 eventos. Interestelar (evento 1) tiene DOS funciones (1 y 3,
-- horarios distintos) que comparten el mismo evento_id: el catalogo
-- (titulo, genero, director, precio) vive una sola vez.
INSERT INTO evento (id, precio, activo) VALUES
    (1, 25.00, TRUE),  -- Interestelar
    (2, 22.00, TRUE),  -- La Casa del Terror
    (3, 35.00, TRUE),  -- La Muralla
    (4, 10.00, TRUE),  -- Charla de Cine Peruano Contemporaneo
    (5, 15.00, TRUE);  -- Taller de Fotografia

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
    (2, 3),           -- Diego reservo A3 (VIP) para la funcion 3
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
