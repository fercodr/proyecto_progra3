-- =====================================================================
-- EntreFunciones — Procedimientos almacenados de la parte 5
-- Equipo: Los que no compilan — Programación 3
-- Entidades a cargo: Pago, Entrada (DAO+BL, con sus IMPL). Reportes ya no
-- es parte de esta entrega.
--
-- Version alineada al schema ACTUAL del repo (con tabla evento y
-- entrada.butaca_id / reserva.cantidad ya NULL-ables para aforo general),
-- pero los procedimientos de aqui abajo no dependen de ese soporte:
-- Reserva.java/Entrada.java del repo todavia solo tienen el constructor
-- de asiento numerado (butaca obligatoria), asi que
-- PagoDAOImpl/EntradaDAOImpl siempre mandan un butaca_id no nulo. Estos
-- procedimientos van a funcionar igual el dia que el equipo agregue el
-- soporte de aforo general (ver dependencias_pendientes_reserva_entrada_
-- NO_ES_TUYO/), porque insertar_entrada ya pasa el parametro tal cual
-- (NULL incluido) al INSERT.
--
-- Este script SOLO trae lo que le corresponde a esta parte de la
-- entrega: los procedimientos que usan PagoDAOImpl y EntradaDAOImpl. No
-- incluye:
--   - CREATE TABLE de nada (las tablas pago/entrada/evento/... ya vienen
--     del script de base de datos del equipo, a cargo de la parte 1).
--   - Procedimientos de otras entidades (sede, sala, recurso, bloqueo,
--     butaca, reserva, pelicula, obra_teatro, actividad_cultural,
--     funcion, usuario, auditoria): esas las trae el script de cada
--     compañero a cargo.
--   - Procedimientos de reportes (reporte_ocupacion,
--     reporte_ingresos_asistencia): ya no son parte de esta entrega.
--
-- Dependencias de otras partes que este script asume que YA EXISTEN en
-- la base de datos al momento de ejecutarlo:
--   - Tablas pago y entrada, tal como estan en sql/entrefunciones-mysql.sql
--     del repo (entrada.butaca_id ya es NULL-able ahi, aunque esta
--     entrega todavia no lo use).
--   - Tabla reserva_butaca (FK compuesta reserva_id+butaca_id): la usa
--     insertar_entrada (FK real de entrada: no se puede insertar una
--     entrada con una butaca si esa reserva no tiene esa butaca en
--     reserva_butaca — eso lo crea quien haga la reserva).
--
-- Al unir los scripts del equipo: este bloque completo (desde el primer
-- DELIMITER $$ hasta el DELIMITER ; final) se pega junto con los de los
-- demás dentro de un mismo bloque de creación de procedimientos, después
-- de que las tablas ya existan.
-- =====================================================================

DELIMITER $$

-- --- pago (una sola tabla, sin IEliminable: eliminado logico via estado) ---

CREATE PROCEDURE listar_pagos()
BEGIN
    SELECT id, reserva_id, codigo_pago, monto, fecha_pago, estado
    FROM pago;
END$$

CREATE PROCEDURE buscar_pago_por_id(IN p_id INT)
BEGIN
    SELECT id, reserva_id, codigo_pago, monto, fecha_pago, estado
    FROM pago
    WHERE id = p_id;
END$$

CREATE PROCEDURE buscar_pago_por_reserva(IN p_reserva_id INT)
BEGIN
    -- A lo mas un pago por reserva (uk_pago_reserva).
    SELECT id, reserva_id, codigo_pago, monto, fecha_pago, estado
    FROM pago
    WHERE reserva_id = p_reserva_id;
END$$

CREATE PROCEDURE buscar_pago_por_codigo(IN p_codigo_pago VARCHAR(50))
BEGIN
    SELECT id, reserva_id, codigo_pago, monto, fecha_pago, estado
    FROM pago
    WHERE codigo_pago = p_codigo_pago;
END$$

CREATE PROCEDURE insertar_pago(
    IN p_reserva_id INT,
    IN p_codigo_pago VARCHAR(50),
    IN p_monto DECIMAL(8,2),
    IN p_fecha_pago DATETIME,
    IN p_estado VARCHAR(20),
    OUT p_id INT
)
BEGIN
    INSERT INTO pago (reserva_id, codigo_pago, monto, fecha_pago, estado)
    VALUES (p_reserva_id, p_codigo_pago, p_monto, p_fecha_pago, p_estado);
    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_pago(
    IN p_id INT,
    IN p_codigo_pago VARCHAR(50),
    IN p_monto DECIMAL(8,2),
    IN p_fecha_pago DATETIME,
    IN p_estado VARCHAR(20)
)
BEGIN
    UPDATE pago
    SET codigo_pago = p_codigo_pago,
        monto = p_monto,
        fecha_pago = p_fecha_pago,
        estado = p_estado
    WHERE id = p_id;
END$$

CREATE PROCEDURE eliminar_pago(IN p_id INT)
BEGIN
    -- Eliminado logico: un pago rechazado nunca se borra (queda como
    -- historial de por que no se completo la venta). Solo pasa a
    -- RECHAZADO si todavia estaba PENDIENTE; un pago ya VALIDADO no se
    -- puede "eliminar" por esta via (refleja el mismo estado que exige
    -- Pago.rechazar() en el dominio). Si la fila no cambia, executeUpdate()
    -- devuelve 0 y el DAO lo reporta como error a la capa de negocio.
    UPDATE pago SET estado = 'RECHAZADO' WHERE id = p_id AND estado = 'PENDIENTE';
END$$

-- --- entrada (una sola tabla, sin IEliminable: eliminado logico via estado) ---

CREATE PROCEDURE listar_entradas()
BEGIN
    SELECT id, reserva_id, butaca_id, fecha_emision, codigo_qr, precio_final, estado
    FROM entrada;
END$$

CREATE PROCEDURE buscar_entrada_por_id(IN p_id INT)
BEGIN
    SELECT id, reserva_id, butaca_id, fecha_emision, codigo_qr, precio_final, estado
    FROM entrada
    WHERE id = p_id;
END$$

CREATE PROCEDURE buscar_entrada_por_reserva(IN p_reserva_id INT)
BEGIN
    -- Una reserva puede tener varias entradas: una por butaca reservada.
    SELECT id, reserva_id, butaca_id, fecha_emision, codigo_qr, precio_final, estado
    FROM entrada
    WHERE reserva_id = p_reserva_id;
END$$

CREATE PROCEDURE buscar_entrada_por_qr(IN p_codigo_qr VARCHAR(100))
BEGIN
    -- Usado por RF-M6-01 para validar el ingreso a partir del codigo QR.
    SELECT id, reserva_id, butaca_id, fecha_emision, codigo_qr, precio_final, estado
    FROM entrada
    WHERE codigo_qr = p_codigo_qr;
END$$

CREATE PROCEDURE insertar_entrada(
    IN p_reserva_id INT,
    IN p_butaca_id INT,
    IN p_fecha_emision DATETIME,
    IN p_codigo_qr VARCHAR(100),
    IN p_precio_final DECIMAL(8,2),
    IN p_estado VARCHAR(20),
    OUT p_id INT
)
BEGIN
    -- fk_entrada_reserva_butaca exige que (p_reserva_id, p_butaca_id) ya
    -- exista como fila en reserva_butaca (la butaca debe haber sido
    -- reservada primero); si no, MySQL rechaza el INSERT con un error de
    -- FK y el DAO lo propaga como SQLException.
    INSERT INTO entrada (reserva_id, butaca_id, fecha_emision, codigo_qr, precio_final, estado)
    VALUES (p_reserva_id, p_butaca_id, p_fecha_emision, p_codigo_qr, p_precio_final, p_estado);
    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_entrada(
    IN p_id INT,
    IN p_estado VARCHAR(20),
    IN p_precio_final DECIMAL(8,2)
)
BEGIN
    -- Butaca/reserva no se reasignan despues de emitida: solo cambia el
    -- estado (validarIngreso/cancelar) o, si hiciera falta, el precio.
    UPDATE entrada
    SET estado = p_estado,
        precio_final = p_precio_final
    WHERE id = p_id;
END$$

CREATE PROCEDURE eliminar_entrada(IN p_id INT)
BEGIN
    -- Eliminado logico: solo cancela si todavia estaba VIGENTE (una
    -- entrada ya UTILIZADA no se puede cancelar retroactivamente).
    UPDATE entrada SET estado = 'CANCELADA' WHERE id = p_id AND estado = 'VIGENTE';
END$$

DELIMITER ;
