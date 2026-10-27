--bloqueo
--Listar todos los bloqueos activos

CREATE PROCEDURE listar_bloqueos()
BEGIN
    SELECT 
        id,
        fecha_inicio,
        fecha_fin,
        motivo,
        activo,
        sala_id,
        recurso_id
    FROM bloqueo
    WHERE activo = 1;
END //

--  Listar bloqueos por ID de sala
CREATE PROCEDURE listar_bloqueos_por_sala(
    IN p_sala_id INT
)
BEGIN
    SELECT 
        id,
        fecha_inicio,
        fecha_fin,
        motivo,
        activo,
        sala_id,
        recurso_id
    FROM bloqueo
    WHERE sala_id = p_sala_id
      AND activo = 1;
END //

-- Listar bloqueos por ID de recurso
CREATE PROCEDURE listar_bloqueos_por_recurso(
    IN p_recurso_id INT
)
BEGIN
    SELECT 
        id,
        fecha_inicio,
        fecha_fin,
        motivo,
        activo,
        sala_id,
        recurso_id
    FROM bloqueo
    WHERE recurso_id = p_recurso_id
      AND activo = 1;
END //

-- 4. Buscar bloqueo por su ID (para el método findById)
CREATE PROCEDURE buscar_bloqueo_por_id(
    IN p_id INT
)
BEGIN
    SELECT 
        id,
        fecha_inicio,
        fecha_fin,
        motivo,
        activo,
        sala_id,
        recurso_id
    FROM bloqueo
    WHERE id = p_id;
END //

-- Insertar un nuevo bloqueo 
CREATE PROCEDURE insertar_bloqueo(
    OUT p_id INT,
    IN p_fecha_inicio DATETIME,
    IN p_fecha_fin DATETIME,
    IN p_motivo VARCHAR(255),
    IN p_sala_id INT,
    IN p_recurso_id INT
)
BEGIN
    INSERT INTO bloqueo (
        fecha_inicio, 
        fecha_fin, 
        motivo, 
        activo, 
        sala_id, 
        recurso_id
    ) VALUES (
        p_fecha_inicio, 
        p_fecha_fin, 
        p_motivo, 
        1, 
        p_sala_id, 
        p_recurso_id
    );
    
    SET p_id = LAST_INSERT_ID();
END //

-- Actualizar un bloqueo existente
CREATE PROCEDURE actualizar_bloqueo(
    IN p_id INT,
    IN p_fecha_inicio DATETIME,
    IN p_fecha_fin DATETIME,
    IN p_motivo VARCHAR(255),
    IN p_sala_id INT,
    IN p_recurso_id INT
)
BEGIN
    UPDATE bloqueo
    SET fecha_inicio = p_fecha_inicio,
        fecha_fin = p_fecha_fin,
        motivo = p_motivo,
        sala_id = p_sala_id,
        recurso_id = p_recurso_id
    WHERE id = p_id;
END //

-- 7. Eliminar lógicamente un bloqueo 
CREATE PROCEDURE eliminar_bloqueo(
    IN p_id INT
)
BEGIN
    UPDATE bloqueo
    SET activo = 0
    WHERE id = p_id;
END //

  
--butaca
-- lista butacas activas

CREATE PROCEDURE listar_butacas()
BEGIN
    SELECT 
        b.id,
        b.fila,
        b.columna,
        b.tipo_butaca,
        b.activo,
        b.sala_id
    FROM butaca b
    WHERE b.activo = 1;
END //

-- listar butacas por ID de sala
CREATE PROCEDURE listar_butacas_por_sala(
    IN p_sala_id INT
)
BEGIN
    SELECT 
        b.id,
        b.fila,
        b.columna,
        b.tipo_butaca,
        b.activo,
        b.sala_id
    FROM butaca b
    WHERE b.sala_id = p_sala_id
      AND b.activo = 1;
END 

-- busca una butaca por su ID
CREATE PROCEDURE buscar_butaca_por_id(
    IN p_id INT
)
BEGIN
    SELECT 
        b.id,
        b.fila,
        b.columna,
        b.tipo_butaca,
        b.activo,
        b.sala_id
    FROM butaca b
    WHERE b.id = p_id;
END 

-- insertar una nueva butaca
CREATE PROCEDURE insertar_butaca(
    IN p_sala_id INT,
    IN p_fila VARCHAR(10),
    IN p_columna INT,
    IN p_tipo_butaca VARCHAR(50),
    IN p_activo TINYINT,
    OUT p_id INT
)
BEGIN
    INSERT INTO butaca (
        sala_id,
        fila,
        columna,
        tipo_butaca,
        activo
    ) VALUES (
        p_sala_id,
        p_fila,
        p_columna,
        p_tipo_butaca,
        p_activo
    );

    SET p_id = LAST_INSERT_ID();
END //

-- modificar una butaca existente
CREATE PROCEDURE modificar_butaca(
    IN p_id INT,
    IN p_sala_id INT,
    IN p_fila VARCHAR(10),
    IN p_columna INT,
    IN p_tipo_butaca VARCHAR(50),
    IN p_activo TINYINT
)
BEGIN
    UPDATE butaca
    SET sala_id = p_sala_id,
        fila = p_fila,
        columna = p_columna,
        tipo_butaca = p_tipo_butaca,
        activo = p_activo
    WHERE id = p_id;
END //

-- elimina una butaca
CREATE PROCEDURE eliminar_butaca(
    IN p_id INT
)
BEGIN
    UPDATE butaca
    SET activo = 0
    WHERE id = p_id;
END //

-- Listar las butacas asociadas a una reserva 
CREATE PROCEDURE listar_butacas_por_reserva(
    IN p_reserva_id INT
)
BEGIN
    SELECT 
        b.id,
        b.fila,
        b.columna,
        b.tipo_butaca,
        b.activo,
        b.sala_id
    FROM butaca b
    INNER JOIN reserva_x_butaca rb ON b.id = rb.butaca_id
    WHERE rb.reserva_id = p_reserva_id
      AND b.activo = 1;
END 
  

--reserva



-- listar todas  reservas

CREATE PROCEDURE listar_reservas()
BEGIN
    SELECT 
        id,
        funcion_id,
        cliente_id,
        fecha_creacion,
        fecha_expiracion,
        estado
    FROM reserva;
END //

-- lista reserva id

CREATE PROCEDURE listar_reservas_por_cliente(
    IN p_cliente_id INT
)
BEGIN
    SELECT 
        id,
        funcion_id,
        cliente_id,
        fecha_creacion,
        fecha_expiracion,
        estado
    FROM reserva
    WHERE cliente_id = p_cliente_id;
END //

-- lista reservas por ID de función
CREATE PROCEDURE listar_reservas_por_funcion(
    IN p_funcion_id INT
)
BEGIN
    SELECT 
        id,
        funcion_id,
        cliente_id,
        fecha_creacion,
        fecha_expiracion,
        estado
    FROM reserva
    WHERE funcion_id = p_funcion_id;
END 

-- busca reserva
CREATE PROCEDURE buscar_reserva_por_id(
    IN p_id INT
)
BEGIN
    SELECT 
        id,
        funcion_id,
        cliente_id,
        fecha_creacion,
        fecha_expiracion,
        estado
    FROM reserva
    WHERE id = p_id;
END //

-- inserta reserava
CREATE PROCEDURE insertar_reserva(
    IN p_funcion_id INT,
    IN p_cliente_id INT,
    IN p_fecha_creacion DATETIME,
    IN p_fecha_expiracion DATETIME,
    IN p_estado VARCHAR(50),
    OUT p_id INT
)
BEGIN
    INSERT INTO reserva (
        funcion_id,
        cliente_id,
        fecha_creacion,
        fecha_expiracion,
        estado
    ) VALUES (
        p_funcion_id,
        p_cliente_id,
        p_fecha_creacion,
        p_fecha_expiracion,
        p_estado
    );

    SET p_id = LAST_INSERT_ID();
END 

-- inserta el detalle en la tabla intermedia 
CREATE PROCEDURE insertar_reserva_butaca(
    IN p_reserva_id INT,
    IN p_butaca_id INT
)
BEGIN
    INSERT INTO reserva_x_butaca (
        reserva_id,
        butaca_id
    ) VALUES (
        p_reserva_id,
        p_butaca_id
    );
END 

-- modificar una reserva existente

CREATE PROCEDURE modificar_reserva(
    IN p_id INT,
    IN p_funcion_id INT,
    IN p_cliente_id INT,
    IN p_fecha_creacion DATETIME,
    IN p_fecha_expiracion DATETIME,
    IN p_estado VARCHAR(50)
)
BEGIN
    UPDATE reserva
    SET funcion_id = p_funcion_id,
        cliente_id = p_cliente_id,
        fecha_creacion = p_fecha_creacion,
        fecha_expiracion = p_fecha_expiracion,
        estado = p_estado
    WHERE id = p_id;
END 

-- eliminar una reserva
DROP PROCEDURE IF EXISTS eliminar_reserva //
CREATE PROCEDURE eliminar_reserva(
    IN p_id INT
)
BEGIN
    DELETE FROM reserva_x_butaca 
    WHERE reserva_id = p_id;

    DELETE FROM reserva 
    WHERE id = p_id;
END 
