-- =====================================================================
-- EntreFunciones — Procedimientos almacenados (ÚNICO script de procedimientos)
-- Equipo: Los que no compilan — Programación 3
--
-- Este archivo reemplaza a los siguientes, que quedan retirados del
-- proyecto (su contenido fue movido/corregido y consolidado aquí):
--   - El bloque de procedimientos que vivía dentro de entrefunciones-mysql.sql
--     (sede, sala, recurso, cliente): ese script vuelve a ser SOLO el que
--     crea la base de datos (tablas, triggers, datos de prueba).
--   - procedimientos-pelicula.sql y procedimientos.obrateatro.sql: escribían
--     contra una tabla "contenido_catalogo" que NUNCA se llegó a crear —
--     era de un diseño anterior (Pelicula/ObraTeatro como subtipos directos
--     de una tabla generica de catálogo), abandonado antes de la entrega
--     porque no resolvía la duplicación de catálogo entre funciones (ver
--     decisiones-diseno-evento.md). El código Java real (EventoDAOImpl) NO
--     llama insertar_pelicula/insertar_obra_teatro en absoluto: llama a
--     insertar_evento + insertar_evento_pelicula/obra_teatro/
--     actividad_cultural, que es lo que se define más abajo.
--   - parte5_pago_entrada.sql (Pago/Entrada): su contenido se copió tal
--     cual, sin cambios de lógica.
--
-- Requiere haber corrido antes sql/entrefunciones-mysql.sql (crea la base
-- de datos "entrefunciones" y todas sus tablas).
--
-- Organizado por paquete de dominio, en el mismo orden que el código Java
-- (usuarios, espacios, catalogo/eventos, ventas). Dentro de cada entidad
-- se sigue el patrón visto en clase: listar_x / buscar_x_por_* /
-- insertar_x / modificar_x / eliminar_x, todo vía CallableStatement.
--
-- ESTRATEGIA TRANSACCIONAL:
--   * insertar/modificar Administrador y Cliente son llamadas autónomas de
--     una sola operación BL y controlan aquí la transacción que abarca sus
--     tablas usuario + subtipo. Sus DAO abren una conexión propia; no deben
--     invocarse dentro de TransactionsManager porque START TRANSACTION haría
--     commit implícito de una transacción MySQL ya activa.
--   * Los procedimientos de eventos, funciones, reservas, pagos y entradas
--     no ejecutan START TRANSACTION, COMMIT ni ROLLBACK. Sus DAO reutilizan
--     la conexión de TransactionsManager y la BL controla la unidad atómica.
-- =====================================================================

USE entrefunciones;

DELIMITER $$

-- =====================================================================
-- PAQUETE usuarios: Administrador, Cliente (tabla por subtipo de Usuario)
-- =====================================================================

-- --- administrador (tabla por subtipo: usuario + administrador) --------
-- Igual que cliente: cada procedimiento que escribe toca usuario y
-- administrador dentro de UNA transacción, para que nunca quede un
-- usuario sin su fila de administrador o viceversa.

CREATE PROCEDURE listar_administradores()
BEGIN
    SELECT u.id, u.nombres, u.apellidos, u.correo, u.contrasena, u.dni, u.activo,
           a.codigo_empleado
    FROM usuario u
    INNER JOIN administrador a ON a.usuario_id = u.id;
END$$

CREATE PROCEDURE buscar_administrador_por_id(IN p_id INT)
BEGIN
    SELECT u.id, u.nombres, u.apellidos, u.correo, u.contrasena, u.dni, u.activo,
           a.codigo_empleado
    FROM usuario u
    INNER JOIN administrador a ON a.usuario_id = u.id
    WHERE u.id = p_id;
END$$

CREATE PROCEDURE buscar_administrador_por_correo(IN p_correo VARCHAR(150))
BEGIN
    SELECT u.id, u.nombres, u.apellidos, u.correo, u.contrasena, u.dni, u.activo,
           a.codigo_empleado
    FROM usuario u
    INNER JOIN administrador a ON a.usuario_id = u.id
    WHERE u.correo = p_correo;
END$$

CREATE PROCEDURE buscar_administrador_por_dni(IN p_dni VARCHAR(15))
BEGIN
    SELECT u.id, u.nombres, u.apellidos, u.correo, u.contrasena, u.dni, u.activo,
           a.codigo_empleado
    FROM usuario u
    INNER JOIN administrador a ON a.usuario_id = u.id
    WHERE u.dni = p_dni;
END$$

CREATE PROCEDURE buscar_administrador_por_codigo(IN p_codigo_empleado VARCHAR(20))
BEGIN
    SELECT u.id, u.nombres, u.apellidos, u.correo, u.contrasena, u.dni, u.activo,
           a.codigo_empleado
    FROM usuario u
    INNER JOIN administrador a ON a.usuario_id = u.id
    WHERE a.codigo_empleado = p_codigo_empleado;
END$$

CREATE PROCEDURE insertar_administrador(
    IN p_nombres VARCHAR(100),
    IN p_apellidos VARCHAR(100),
    IN p_correo VARCHAR(150),
    IN p_contrasena VARCHAR(255),
    IN p_dni VARCHAR(15),
    IN p_activo BOOLEAN,
    IN p_codigo_empleado VARCHAR(20),
    OUT p_id INT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    INSERT INTO usuario (nombres, apellidos, correo, contrasena, dni, activo)
    VALUES (p_nombres, p_apellidos, p_correo, p_contrasena, p_dni, p_activo);
    SET p_id = LAST_INSERT_ID();

    INSERT INTO administrador (usuario_id, codigo_empleado)
    VALUES (p_id, p_codigo_empleado);

    COMMIT;
END$$

CREATE PROCEDURE modificar_administrador(
    IN p_nombres VARCHAR(100),
    IN p_apellidos VARCHAR(100),
    IN p_correo VARCHAR(150),
    IN p_contrasena VARCHAR(255),
    IN p_dni VARCHAR(15),
    IN p_activo BOOLEAN,
    IN p_codigo_empleado VARCHAR(20),
    IN p_id INT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    UPDATE usuario
    SET nombres = p_nombres,
        apellidos = p_apellidos,
        correo = p_correo,
        contrasena = p_contrasena,
        dni = p_dni,
        activo = p_activo
    WHERE id = p_id;

    UPDATE administrador
    SET codigo_empleado = p_codigo_empleado
    WHERE usuario_id = p_id;

    COMMIT;
END$$

CREATE PROCEDURE eliminar_administrador(IN p_id INT)
BEGIN
    -- Eliminado logico: solo se desactiva la fila base en usuario.
    UPDATE usuario SET activo = FALSE WHERE id = p_id;
END$$

-- --- cliente (tabla por subtipo: usuario + cliente) ---------------------
-- Idéntico al original de entrefunciones-mysql.sql; se agrega únicamente
-- buscar_cliente_por_dni, que UsuarioDAOImpl<T>.findByDni() ya necesita
-- pero que no existía en ningún script.

CREATE PROCEDURE listar_clientes()
BEGIN
    SELECT u.id, u.nombres, u.apellidos, u.correo, u.contrasena, u.dni, u.activo,
           c.fecha_registro, c.telefono
    FROM usuario u
    INNER JOIN cliente c ON c.usuario_id = u.id;
END$$

CREATE PROCEDURE buscar_cliente_por_id(IN p_id INT)
BEGIN
    SELECT u.id, u.nombres, u.apellidos, u.correo, u.contrasena, u.dni, u.activo,
           c.fecha_registro, c.telefono
    FROM usuario u
    INNER JOIN cliente c ON c.usuario_id = u.id
    WHERE u.id = p_id;
END$$

CREATE PROCEDURE buscar_cliente_por_correo(IN p_correo VARCHAR(150))
BEGIN
    SELECT u.id, u.nombres, u.apellidos, u.correo, u.contrasena, u.dni, u.activo,
           c.fecha_registro, c.telefono
    FROM usuario u
    INNER JOIN cliente c ON c.usuario_id = u.id
    WHERE u.correo = p_correo;
END$$

CREATE PROCEDURE buscar_cliente_por_dni(IN p_dni VARCHAR(15))
BEGIN
    SELECT u.id, u.nombres, u.apellidos, u.correo, u.contrasena, u.dni, u.activo,
           c.fecha_registro, c.telefono
    FROM usuario u
    INNER JOIN cliente c ON c.usuario_id = u.id
    WHERE u.dni = p_dni;
END$$

CREATE PROCEDURE insertar_cliente(
    IN p_nombres VARCHAR(100),
    IN p_apellidos VARCHAR(100),
    IN p_correo VARCHAR(150),
    IN p_contrasena VARCHAR(255),
    IN p_dni VARCHAR(15),
    IN p_activo BOOLEAN,
    IN p_fecha_registro DATE,
    IN p_telefono VARCHAR(20),
    OUT p_id INT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    INSERT INTO usuario (nombres, apellidos, correo, contrasena, dni, activo)
    VALUES (p_nombres, p_apellidos, p_correo, p_contrasena, p_dni, p_activo);
    SET p_id = LAST_INSERT_ID();

    INSERT INTO cliente (usuario_id, fecha_registro, telefono)
    VALUES (p_id, p_fecha_registro, p_telefono);

    COMMIT;
END$$

CREATE PROCEDURE modificar_cliente(
    IN p_id INT,
    IN p_nombres VARCHAR(100),
    IN p_apellidos VARCHAR(100),
    IN p_correo VARCHAR(150),
    IN p_contrasena VARCHAR(255),
    IN p_dni VARCHAR(15),
    IN p_activo BOOLEAN,
    IN p_fecha_registro DATE,
    IN p_telefono VARCHAR(20)
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    UPDATE usuario
    SET nombres = p_nombres,
        apellidos = p_apellidos,
        correo = p_correo,
        contrasena = p_contrasena,
        dni = p_dni,
        activo = p_activo
    WHERE id = p_id;

    UPDATE cliente
    SET fecha_registro = p_fecha_registro,
        telefono = p_telefono
    WHERE usuario_id = p_id;

    COMMIT;
END$$

CREATE PROCEDURE eliminar_cliente(IN p_id INT)
BEGIN
    -- Eliminado logico: solo se desactiva la fila base en usuario; la
    -- fila de cliente no se toca (sigue existiendo el historial).
    UPDATE usuario SET activo = FALSE WHERE id = p_id;
END$$

-- =====================================================================
-- PAQUETE espacios: Sede, Sala, Recurso
-- (Butaca y Bloqueo ya NO usan procedimientos almacenados: su DAOImpl
-- actual arma SQL directo con ConnectionScope/TransactionsManager, así
-- que no hay nada que definir aquí para esas dos entidades.)
-- =====================================================================

-- --- sede (entidad de una sola tabla) --------------------------------

CREATE PROCEDURE listar_sedes()
BEGIN
    SELECT id, razon_social, direccion, ciudad, telefono_contacto, activo
    FROM sede;
END$$

CREATE PROCEDURE buscar_sede_por_id(IN p_id INT)
BEGIN
    SELECT id, razon_social, direccion, ciudad, telefono_contacto, activo
    FROM sede
    WHERE id = p_id;
END$$

CREATE PROCEDURE buscar_sede_por_razon_social(IN p_razon_social VARCHAR(150))
BEGIN
    SELECT id, razon_social, direccion, ciudad, telefono_contacto, activo
    FROM sede
    WHERE razon_social = p_razon_social;
END$$

CREATE PROCEDURE insertar_sede(
    IN p_razon_social VARCHAR(150),
    IN p_direccion VARCHAR(200),
    IN p_ciudad VARCHAR(80),
    IN p_telefono_contacto VARCHAR(20),
    IN p_activo BOOLEAN,
    OUT p_id INT
)
BEGIN
    INSERT INTO sede (razon_social, direccion, ciudad, telefono_contacto, activo)
    VALUES (p_razon_social, p_direccion, p_ciudad, p_telefono_contacto, p_activo);
    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_sede(
    IN p_id INT,
    IN p_razon_social VARCHAR(150),
    IN p_direccion VARCHAR(200),
    IN p_ciudad VARCHAR(80),
    IN p_telefono_contacto VARCHAR(20),
    IN p_activo BOOLEAN
)
BEGIN
    UPDATE sede
    SET razon_social = p_razon_social,
        direccion = p_direccion,
        ciudad = p_ciudad,
        telefono_contacto = p_telefono_contacto,
        activo = p_activo
    WHERE id = p_id;
END$$

CREATE PROCEDURE eliminar_sede(IN p_id INT)
BEGIN
    -- Eliminado logico: nunca se borra la fila, solo se desactiva.
    UPDATE sede SET activo = FALSE WHERE id = p_id;
END$$

-- --- sala ------------------------------------------------------------

CREATE PROCEDURE listar_salas()
BEGIN
    SELECT sa.id, sa.nombre, sa.aforo, sa.tipo_sala, sa.activo,
           sa.sede_id,
           se.razon_social AS sede_razon_social,
           se.direccion AS sede_direccion,
           se.ciudad AS sede_ciudad,
           se.telefono_contacto AS sede_telefono_contacto,
           se.activo AS sede_activo
    FROM sala sa
    INNER JOIN sede se ON se.id = sa.sede_id
    ORDER BY sa.id;
END$$

CREATE PROCEDURE buscar_sala_por_id(IN p_id INT)
BEGIN
    SELECT sa.id, sa.nombre, sa.aforo, sa.tipo_sala, sa.activo,
           sa.sede_id,
           se.razon_social AS sede_razon_social,
           se.direccion AS sede_direccion,
           se.ciudad AS sede_ciudad,
           se.telefono_contacto AS sede_telefono_contacto,
           se.activo AS sede_activo
    FROM sala sa
    INNER JOIN sede se ON se.id = sa.sede_id
    WHERE sa.id = p_id;
END$$

CREATE PROCEDURE listar_salas_por_sede(IN p_sede_id INT)
BEGIN
    SELECT sa.id, sa.nombre, sa.aforo, sa.tipo_sala, sa.activo,
           sa.sede_id,
           se.razon_social AS sede_razon_social,
           se.direccion AS sede_direccion,
           se.ciudad AS sede_ciudad,
           se.telefono_contacto AS sede_telefono_contacto,
           se.activo AS sede_activo
    FROM sala sa
    INNER JOIN sede se ON se.id = sa.sede_id
    WHERE sa.sede_id = p_sede_id
    ORDER BY sa.id;
END$$

CREATE PROCEDURE insertar_sala(
    IN p_sede_id INT,
    IN p_nombre VARCHAR(100),
    IN p_aforo INT,
    IN p_tipo_sala VARCHAR(50),
    IN p_activo BOOLEAN,
    OUT p_id INT
)
BEGIN
    INSERT INTO sala (sede_id, nombre, aforo, tipo_sala, activo)
    VALUES (p_sede_id, p_nombre, p_aforo, p_tipo_sala, p_activo);
    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_sala(
    IN p_id INT,
    IN p_sede_id INT,
    IN p_nombre VARCHAR(100),
    IN p_aforo INT,
    IN p_tipo_sala VARCHAR(50),
    IN p_activo BOOLEAN
)
BEGIN
    UPDATE sala
    SET sede_id = p_sede_id,
        nombre = p_nombre,
        aforo = p_aforo,
        tipo_sala = p_tipo_sala,
        activo = p_activo
    WHERE id = p_id;
END$$

CREATE PROCEDURE eliminar_sala(IN p_id INT)
BEGIN
    UPDATE sala SET activo = FALSE WHERE id = p_id;
END$$

-- --- recurso ---------------------------------------------------------

CREATE PROCEDURE listar_recursos()
BEGIN
    SELECT id, nombre, tipo, activo
    FROM recurso
    ORDER BY id;
END$$

CREATE PROCEDURE buscar_recurso_por_id(IN p_id INT)
BEGIN
    SELECT id, nombre, tipo, activo
    FROM recurso
    WHERE id = p_id;
END$$

CREATE PROCEDURE insertar_recurso(
    IN p_nombre VARCHAR(100),
    IN p_tipo VARCHAR(50),
    IN p_activo BOOLEAN,
    OUT p_id INT
)
BEGIN
    INSERT INTO recurso (nombre, tipo, activo)
    VALUES (p_nombre, p_tipo, p_activo);
    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_recurso(
    IN p_id INT,
    IN p_nombre VARCHAR(100),
    IN p_tipo VARCHAR(50),
    IN p_activo BOOLEAN
)
BEGIN
    UPDATE recurso
    SET nombre = p_nombre,
        tipo = p_tipo,
        activo = p_activo
    WHERE id = p_id;
END$$

CREATE PROCEDURE eliminar_recurso(IN p_id INT)
BEGIN
    UPDATE recurso SET activo = FALSE WHERE id = p_id;
END$$

-- =====================================================================
-- PAQUETE catalogo/eventos: Evento (base) + Pelicula/ObraTeatro/
-- ActividadCultural (subtipos, tabla por subtipo vía evento_id) + Funcion
-- (la programación puntual de un Evento en una sala/fecha/horario).
--
-- listar_eventos/buscar_evento_por_id devuelven una fila "aplanada" con
-- un LEFT JOIN a las 3 tablas de subtipo y una columna discriminadora
-- tipo_evento (PELICULA/OBRA_TEATRO/ACTIVIDAD_CULTURAL), que es lo que
-- EventoDAOImpl.construir(rs) espera para decidir qué subclase instanciar.
-- =====================================================================

CREATE PROCEDURE listar_eventos()
BEGIN
    SELECT e.id, e.precio, e.activo,
           CASE
               WHEN p.evento_id IS NOT NULL THEN 'PELICULA'
               WHEN o.evento_id IS NOT NULL THEN 'OBRA_TEATRO'
               WHEN a.evento_id IS NOT NULL THEN 'ACTIVIDAD_CULTURAL'
           END AS tipo_evento,
           p.nombre_pelicula, p.duracion_minutos, p.genero, p.clasificacion_edad, p.director,
           o.titulo, o.nombre_director, o.elenco,
           a.nombre, a.expositor, a.tipo_actividad
    FROM evento e
    LEFT JOIN pelicula p ON p.evento_id = e.id
    LEFT JOIN obra_teatro o ON o.evento_id = e.id
    LEFT JOIN actividad_cultural a ON a.evento_id = e.id
    ORDER BY e.id;
END$$

CREATE PROCEDURE buscar_evento_por_id(IN p_id INT)
BEGIN
    SELECT e.id, e.precio, e.activo,
           CASE
               WHEN p.evento_id IS NOT NULL THEN 'PELICULA'
               WHEN o.evento_id IS NOT NULL THEN 'OBRA_TEATRO'
               WHEN a.evento_id IS NOT NULL THEN 'ACTIVIDAD_CULTURAL'
           END AS tipo_evento,
           p.nombre_pelicula, p.duracion_minutos, p.genero, p.clasificacion_edad, p.director,
           o.titulo, o.nombre_director, o.elenco,
           a.nombre, a.expositor, a.tipo_actividad
    FROM evento e
    LEFT JOIN pelicula p ON p.evento_id = e.id
    LEFT JOIN obra_teatro o ON o.evento_id = e.id
    LEFT JOIN actividad_cultural a ON a.evento_id = e.id
    WHERE e.id = p_id;
END$$

CREATE PROCEDURE insertar_evento(
    IN p_precio DECIMAL(8,2),
    IN p_activo BOOLEAN,
    OUT p_id INT
)
BEGIN
    INSERT INTO evento (precio, activo) VALUES (p_precio, p_activo);
    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_evento(
    IN p_id INT,
    IN p_precio DECIMAL(8,2),
    IN p_activo BOOLEAN
)
BEGIN
    UPDATE evento SET precio = p_precio, activo = p_activo WHERE id = p_id;
END$$

CREATE PROCEDURE eliminar_evento(IN p_id INT)
BEGIN
    -- Eliminado logico; EventoDAOImpl.delete() ademas cancela todas las
    -- funciones de este evento en la misma transaccion (ver
    -- cancelar_funciones_por_evento mas abajo).
    UPDATE evento SET activo = FALSE WHERE id = p_id;
END$$

-- --- pelicula (subtipo de evento) ---------------------------------------

CREATE PROCEDURE insertar_evento_pelicula(
    IN p_evento_id INT,
    IN p_nombre_pelicula VARCHAR(150),
    IN p_duracion_minutos INT,
    IN p_genero VARCHAR(20),
    IN p_clasificacion_edad VARCHAR(20),
    IN p_director VARCHAR(100)
)
BEGIN
    INSERT INTO pelicula (evento_id, nombre_pelicula, duracion_minutos, genero, clasificacion_edad, director)
    VALUES (p_evento_id, p_nombre_pelicula, p_duracion_minutos, p_genero, p_clasificacion_edad, p_director);
END$$

CREATE PROCEDURE modificar_evento_pelicula(
    IN p_evento_id INT,
    IN p_nombre_pelicula VARCHAR(150),
    IN p_duracion_minutos INT,
    IN p_genero VARCHAR(20),
    IN p_clasificacion_edad VARCHAR(20),
    IN p_director VARCHAR(100)
)
BEGIN
    UPDATE pelicula
    SET nombre_pelicula = p_nombre_pelicula,
        duracion_minutos = p_duracion_minutos,
        genero = p_genero,
        clasificacion_edad = p_clasificacion_edad,
        director = p_director
    WHERE evento_id = p_evento_id;
END$$

-- --- obra_teatro (subtipo de evento) -------------------------------------

CREATE PROCEDURE insertar_evento_obra_teatro(
    IN p_evento_id INT,
    IN p_titulo VARCHAR(150),
    IN p_nombre_director VARCHAR(100),
    IN p_elenco VARCHAR(300)
)
BEGIN
    INSERT INTO obra_teatro (evento_id, titulo, nombre_director, elenco)
    VALUES (p_evento_id, p_titulo, p_nombre_director, p_elenco);
END$$

CREATE PROCEDURE modificar_evento_obra_teatro(
    IN p_evento_id INT,
    IN p_titulo VARCHAR(150),
    IN p_nombre_director VARCHAR(100),
    IN p_elenco VARCHAR(300)
)
BEGIN
    UPDATE obra_teatro
    SET titulo = p_titulo,
        nombre_director = p_nombre_director,
        elenco = p_elenco
    WHERE evento_id = p_evento_id;
END$$

-- --- actividad_cultural (subtipo de evento) ------------------------------

CREATE PROCEDURE insertar_evento_actividad_cultural(
    IN p_evento_id INT,
    IN p_nombre VARCHAR(150),
    IN p_expositor VARCHAR(100),
    IN p_tipo_actividad VARCHAR(20)
)
BEGIN
    INSERT INTO actividad_cultural (evento_id, nombre, expositor, tipo_actividad)
    VALUES (p_evento_id, p_nombre, p_expositor, p_tipo_actividad);
END$$

CREATE PROCEDURE modificar_evento_actividad_cultural(
    IN p_evento_id INT,
    IN p_nombre VARCHAR(150),
    IN p_expositor VARCHAR(100),
    IN p_tipo_actividad VARCHAR(20)
)
BEGIN
    UPDATE actividad_cultural
    SET nombre = p_nombre,
        expositor = p_expositor,
        tipo_actividad = p_tipo_actividad
    WHERE evento_id = p_evento_id;
END$$

-- --- funcion (programacion puntual de un evento en sala/fecha/horario) --

CREATE PROCEDURE listar_funciones()
BEGIN
    SELECT id, evento_id, sala_id, fecha, hora_inicio, hora_fin, idioma, estado
    FROM funcion
    ORDER BY id;
END$$

CREATE PROCEDURE listar_funciones_por_evento(IN p_evento_id INT)
BEGIN
    SELECT id, evento_id, sala_id, fecha, hora_inicio, hora_fin, idioma, estado
    FROM funcion
    WHERE evento_id = p_evento_id
    ORDER BY id;
END$$

CREATE PROCEDURE listar_funciones_por_sala(IN p_sala_id INT)
BEGIN
    SELECT id, evento_id, sala_id, fecha, hora_inicio, hora_fin, idioma, estado
    FROM funcion
    WHERE sala_id = p_sala_id
    ORDER BY id;
END$$

CREATE PROCEDURE buscar_funcion_por_id(IN p_id INT)
BEGIN
    SELECT id, evento_id, sala_id, fecha, hora_inicio, hora_fin, idioma, estado
    FROM funcion
    WHERE id = p_id;
END$$

CREATE PROCEDURE insertar_funcion(
    IN p_evento_id INT,
    IN p_sala_id INT,
    IN p_fecha DATE,
    IN p_hora_inicio TIME,
    IN p_hora_fin TIME,
    IN p_idioma VARCHAR(30),
    IN p_estado VARCHAR(20),
    OUT p_id INT
)
BEGIN
    INSERT INTO funcion (evento_id, sala_id, fecha, hora_inicio, hora_fin, idioma, estado)
    VALUES (p_evento_id, p_sala_id, p_fecha, p_hora_inicio, p_hora_fin, p_idioma, p_estado);
    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_funcion(
    IN p_id INT,
    IN p_sala_id INT,
    IN p_fecha DATE,
    IN p_hora_inicio TIME,
    IN p_hora_fin TIME,
    IN p_idioma VARCHAR(30),
    IN p_estado VARCHAR(20)
)
BEGIN
    UPDATE funcion
    SET sala_id = p_sala_id,
        fecha = p_fecha,
        hora_inicio = p_hora_inicio,
        hora_fin = p_hora_fin,
        idioma = p_idioma,
        estado = p_estado
    WHERE id = p_id;
END$$

CREATE PROCEDURE cancelar_funcion(IN p_id INT)
BEGIN
    UPDATE funcion SET estado = 'CANCELADA' WHERE id = p_id;
END$$

CREATE PROCEDURE cancelar_funciones_por_evento(IN p_evento_id INT)
BEGIN
    UPDATE funcion SET estado = 'CANCELADA' WHERE evento_id = p_evento_id;
END$$

-- =====================================================================
-- PAQUETE ventas: Reserva, Pago, Entrada
-- =====================================================================

-- --- reserva -------------------------------------------------------------
-- cantidad viene NULL en una reserva de asiento numerado (Pelicula,
-- ObraTeatro: se calcula de las filas de reserva_butaca) y con valor en
-- una de aforo general (ActividadCultural, sin butacas puntuales) — ver
-- decisiones-diseno-aforo-general.md y el trigger trg_reserva_tipo_y_aforo
-- mas arriba, que es quien realmente hace cumplir esta regla.

CREATE PROCEDURE listar_reservas()
BEGIN
    SELECT id, funcion_id, cliente_id, cantidad, fecha_creacion, fecha_expiracion, estado
    FROM reserva
    ORDER BY id;
END$$

CREATE PROCEDURE listar_reservas_por_cliente(IN p_cliente_id INT)
BEGIN
    SELECT id, funcion_id, cliente_id, cantidad, fecha_creacion, fecha_expiracion, estado
    FROM reserva
    WHERE cliente_id = p_cliente_id
    ORDER BY id;
END$$

CREATE PROCEDURE listar_reservas_por_funcion(IN p_funcion_id INT)
BEGIN
    SELECT id, funcion_id, cliente_id, cantidad, fecha_creacion, fecha_expiracion, estado
    FROM reserva
    WHERE funcion_id = p_funcion_id
    ORDER BY id;
END$$

CREATE PROCEDURE buscar_reserva_por_id(IN p_id INT)
BEGIN
    SELECT id, funcion_id, cliente_id, cantidad, fecha_creacion, fecha_expiracion, estado
    FROM reserva
    WHERE id = p_id;
END$$

CREATE PROCEDURE insertar_reserva(
    IN p_funcion_id INT,
    IN p_cliente_id INT,
    IN p_cantidad INT,
    IN p_fecha_creacion DATETIME,
    IN p_fecha_expiracion DATETIME,
    IN p_estado VARCHAR(20),
    OUT p_id INT
)
BEGIN
    -- p_cantidad debe llegar NULL en asiento numerado y con valor en aforo
    -- general; trg_reserva_tipo_y_aforo (ver arriba) rechaza el INSERT si
    -- no corresponde con el tipo de Evento de la funcion.
    INSERT INTO reserva (funcion_id, cliente_id, cantidad, fecha_creacion, fecha_expiracion, estado)
    VALUES (p_funcion_id, p_cliente_id, p_cantidad, p_fecha_creacion, p_fecha_expiracion, p_estado);
    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE insertar_reserva_butaca(
    IN p_reserva_id INT,
    IN p_butaca_id INT
)
BEGIN
    INSERT INTO reserva_butaca (reserva_id, butaca_id)
    VALUES (p_reserva_id, p_butaca_id);
END$$

CREATE PROCEDURE modificar_reserva(
    IN p_id INT,
    IN p_funcion_id INT,
    IN p_cliente_id INT,
    IN p_cantidad INT,
    IN p_fecha_creacion DATETIME,
    IN p_fecha_expiracion DATETIME,
    IN p_estado VARCHAR(20)
)
BEGIN
    -- Nota: a diferencia del INSERT, esta actualizacion no dispara
    -- trg_reserva_tipo_y_aforo (ese trigger es BEFORE INSERT). En la
    -- practica ReservaDAOImpl.update() siempre reenvia el mismo valor de
    -- cantidad con el que se creo la reserva (una reserva no cambia de
    -- asiento numerado a aforo general ni viceversa), asi que esto no
    -- abre una via para violar la regla — solo queda documentado por si
    -- alguien llama a este procedimiento directamente por SQL.
    UPDATE reserva
    SET funcion_id = p_funcion_id,
        cliente_id = p_cliente_id,
        cantidad = p_cantidad,
        fecha_creacion = p_fecha_creacion,
        fecha_expiracion = p_fecha_expiracion,
        estado = p_estado
    WHERE id = p_id;
END$$

CREATE PROCEDURE eliminar_reserva(IN p_id INT)
BEGIN
    -- Eliminado logico, igual que Reserva.cancelar() en el dominio: solo
    -- cancela si todavia estaba ACTIVA.
    UPDATE reserva SET estado = 'CANCELADA' WHERE id = p_id AND estado = 'ACTIVA';
END$$

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
    -- FK y el DAO lo propaga como SQLException. p_butaca_id puede ser
    -- NULL (aforo general): esa FK compuesta no se evalua si alguna de
    -- sus columnas es NULL.
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
