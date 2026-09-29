-- =====================================================================
-- EntreFunciones — Script de base de datos (MySQL 8.0 / MariaDB 10.5+)
-- Equipo: Los que no compilan — Programación 3
--
-- Deriva directamente del modelo de dominio en Java (paquetes comun,
-- usuarios, espacios, catalogo, funciones, ventas, auditoria). Decisiones
-- de mapeo objeto-relacional documentadas junto a cada tabla:
--
--   - Herencia Usuario/Administrador/Cliente y
--     Evento/Pelicula/ObraTeatro/ActividadCultural: tabla por subtipo (la
--     tabla base guarda los atributos comunes; cada subtipo comparte su
--     clave primaria con la base mediante una FK 1 a 1).
--   - Funcion no hereda de Evento: cada Funcion referencia al Evento que
--     programa mediante evento_id (FK simple, no comparte clave primaria),
--     para que varias funciones (horarios distintos) puedan compartir el
--     mismo Evento sin repetir su catalogo.
--   - Enumeraciones Java (EstadoFuncion, EstadoReserva, EstadoPago,
--     EstadoEntrada, EstadoAuditoria, CategoriaGenero, ClasificacionEdad,
--     TipoActividad): tipo ENUM nativo de MySQL, con los mismos valores
--     definidos en el código.
--   - Reserva "1" --> "*" Butaca: tabla puente reserva_butaca.
--   - Entrada "*" --> "1" Reserva y "*" --> "1" Butaca: en el modelo de
--     dominio son dos asociaciones independientes, pero una Entrada
--     siempre corresponde a una butaca que efectivamente fue parte de la
--     Reserva que la origina; por eso entrada referencia el PAR
--     (reserva_id, butaca_id) con una FK compuesta hacia reserva_butaca,
--     en lugar de dos FK sueltas. Esto impide emitir una entrada para una
--     butaca que nunca se reservó.
--   - NOTA: al diseñar este script se detectaron relaciones del diagrama
--     de clases que el código Java todavía no implementaba (Funcion sin
--     atributo `sala`, Auditoria sin forma de indicar a qué Funcion
--     pertenece una incidencia, entre otras). Ya se corrigieron en el
--     módulo de dominio (entrefunciones-modelo); columnas como
--     funcion.sala_id y auditoria.funcion_id de este script tienen su
--     equivalente en Java.
-- =====================================================================

DROP DATABASE IF EXISTS entrefunciones;
CREATE DATABASE entrefunciones CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE entrefunciones;

-- ---------------------------------------------------------------------
-- Paquete usuarios: Usuario (abstracta) + Administrador / Cliente
-- ---------------------------------------------------------------------

CREATE TABLE usuario (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    nombres       VARCHAR(100)  NOT NULL,
    apellidos     VARCHAR(100)  NOT NULL,
    correo        VARCHAR(150)  NOT NULL,
    contrasena    VARCHAR(255)  NOT NULL,
    dni           VARCHAR(15)   NOT NULL,
    activo        BOOLEAN       NOT NULL DEFAULT TRUE,
    UNIQUE KEY uk_usuario_correo (correo),
    UNIQUE KEY uk_usuario_dni (dni)
);

-- Extiende usuario (tabla por subtipo): un Administrador SIEMPRE es un Usuario.
CREATE TABLE administrador (
    usuario_id      INT PRIMARY KEY,
    codigo_empleado VARCHAR(20) NOT NULL,
    UNIQUE KEY uk_administrador_codigo (codigo_empleado),
    CONSTRAINT fk_administrador_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuario (id) ON DELETE CASCADE
);

CREATE TABLE cliente (
    usuario_id     INT PRIMARY KEY,
    fecha_registro DATE        NOT NULL,
    telefono       VARCHAR(20),
    CONSTRAINT fk_cliente_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuario (id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- Paquete espacios: Sede, Sala, Butaca, Recurso, Bloqueo
-- ---------------------------------------------------------------------

CREATE TABLE sede (
    id                 INT AUTO_INCREMENT PRIMARY KEY,
    razon_social       VARCHAR(150) NOT NULL,
    direccion          VARCHAR(200) NOT NULL,
    ciudad             VARCHAR(100) NOT NULL,
    telefono_contacto  VARCHAR(20),
    activo             BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE sala (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    sede_id    INT          NOT NULL,
    nombre     VARCHAR(100) NOT NULL,
    aforo      INT          NOT NULL,
    tipo_sala  VARCHAR(50)  NOT NULL,
    activo     BOOLEAN      NOT NULL DEFAULT TRUE,
    UNIQUE KEY uk_sala_sede_nombre (sede_id, nombre),
    CONSTRAINT fk_sala_sede FOREIGN KEY (sede_id) REFERENCES sede (id),
    CONSTRAINT chk_sala_aforo CHECK (aforo > 0)
);

CREATE TABLE butaca (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    sala_id      INT         NOT NULL,
    fila         VARCHAR(5)  NOT NULL,
    columna      INT         NOT NULL,
    tipo_butaca  VARCHAR(30) NOT NULL,
    activo       BOOLEAN     NOT NULL DEFAULT TRUE,
    UNIQUE KEY uk_butaca_sala_pos (sala_id, fila, columna),
    CONSTRAINT fk_butaca_sala FOREIGN KEY (sala_id) REFERENCES sala (id)
);

CREATE TABLE recurso (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    nombre  VARCHAR(100) NOT NULL,
    tipo    VARCHAR(50)  NOT NULL,
    activo  BOOLEAN      NOT NULL DEFAULT TRUE
);

-- Bloquea una Sala O un Recurso (nunca ambos, nunca ninguno): RF-M2-02.
CREATE TABLE bloqueo (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    motivo        VARCHAR(200)  NOT NULL,
    fecha_inicio  DATETIME      NOT NULL,
    fecha_fin     DATETIME      NOT NULL,
    sala_id       INT           NULL,
    recurso_id    INT           NULL,
    CONSTRAINT fk_bloqueo_sala FOREIGN KEY (sala_id) REFERENCES sala (id),
    CONSTRAINT fk_bloqueo_recurso FOREIGN KEY (recurso_id) REFERENCES recurso (id),
    CONSTRAINT chk_bloqueo_fechas CHECK (fecha_fin > fecha_inicio),
    CONSTRAINT chk_bloqueo_objetivo_unico CHECK (
        (sala_id IS NOT NULL AND recurso_id IS NULL) OR
        (sala_id IS NULL AND recurso_id IS NOT NULL)
    )
);

-- ---------------------------------------------------------------------
-- Paquete catalogo: Evento (tabla BASE de la herencia Evento ->
-- Pelicula/ObraTeatro/ActividadCultural). Guarda lo que es propio del
-- contenido en si (precio, activo/IEliminable) y no de una programacion
-- puntual, que es responsabilidad de Funcion (ver mas abajo).
-- ---------------------------------------------------------------------

CREATE TABLE evento (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    precio  DECIMAL(8,2) NOT NULL,
    activo  BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_evento_precio CHECK (precio >= 0)
);

CREATE TABLE pelicula (
    evento_id          INT PRIMARY KEY,
    nombre_pelicula    VARCHAR(150) NOT NULL,
    duracion_minutos   INT          NOT NULL,
    genero             ENUM('ACCION','COMEDIA','DRAMA','TERROR','ANIMACION','DOCUMENTAL') NOT NULL,
    clasificacion_edad ENUM('APTA_TODO_PUBLICO','PG13','MAYORES_18') NOT NULL,
    director           VARCHAR(100),
    CONSTRAINT fk_pelicula_evento FOREIGN KEY (evento_id)
        REFERENCES evento (id) ON DELETE CASCADE,
    CONSTRAINT chk_pelicula_duracion CHECK (duracion_minutos > 0)
);

CREATE TABLE obra_teatro (
    evento_id        INT PRIMARY KEY,
    titulo           VARCHAR(150) NOT NULL,
    nombre_director  VARCHAR(100),
    elenco           VARCHAR(300),
    CONSTRAINT fk_obra_teatro_evento FOREIGN KEY (evento_id)
        REFERENCES evento (id) ON DELETE CASCADE
);

CREATE TABLE actividad_cultural (
    evento_id       INT PRIMARY KEY,
    nombre          VARCHAR(150) NOT NULL,
    expositor       VARCHAR(100),
    tipo_actividad  ENUM('CHARLA','TALLER','EXPOSICION','CONVERSATORIO') NOT NULL,
    CONSTRAINT fk_actividad_cultural_evento FOREIGN KEY (evento_id)
        REFERENCES evento (id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- Paquete funciones: Funcion es la programacion puntual de un Evento en
-- una sala, fecha y horario. Varias Funcion pueden compartir el mismo
-- evento_id (el mismo contenido en mas de un horario) sin repetir su
-- catalogo, que vive una sola vez en evento/pelicula/obra_teatro/
-- actividad_cultural.
-- ---------------------------------------------------------------------

CREATE TABLE funcion (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    evento_id     INT           NOT NULL,
    sala_id       INT           NOT NULL,
    fecha         DATE          NOT NULL,
    hora_inicio   TIME          NOT NULL,
    hora_fin      TIME          NOT NULL,
    idioma        VARCHAR(30)   NOT NULL,
    estado        ENUM('PROGRAMADA','CONFIRMADA','CANCELADA') NOT NULL DEFAULT 'PROGRAMADA',
    CONSTRAINT fk_funcion_evento FOREIGN KEY (evento_id) REFERENCES evento (id),
    CONSTRAINT fk_funcion_sala FOREIGN KEY (sala_id) REFERENCES sala (id),
    CONSTRAINT chk_funcion_horario CHECK (hora_fin > hora_inicio)
);

CREATE INDEX ix_funcion_fecha ON funcion (fecha);
CREATE INDEX ix_funcion_sala_fecha ON funcion (sala_id, fecha);
CREATE INDEX ix_funcion_evento ON funcion (evento_id);

-- ---------------------------------------------------------------------
-- Paquete ventas: Reserva, Pago, Entrada
-- ---------------------------------------------------------------------

-- `cantidad` solo aplica a una reserva de aforo general (actividad_cultural):
-- una reserva de asiento numerado deja `cantidad` en NULL y su conteo real
-- se obtiene de sus filas en reserva_butaca (ver trigger mas abajo, que
-- valida cual de los dos casos corresponde segun el tipo de la funcion).
CREATE TABLE reserva (
    id                 INT AUTO_INCREMENT PRIMARY KEY,
    funcion_id         INT       NOT NULL,
    cliente_id         INT       NOT NULL,
    cantidad           INT       NULL,
    fecha_creacion     DATETIME  NOT NULL,
    fecha_expiracion   DATETIME  NOT NULL,
    estado             ENUM('ACTIVA','CONFIRMADA','EXPIRADA','CANCELADA') NOT NULL DEFAULT 'ACTIVA',
    CONSTRAINT fk_reserva_funcion FOREIGN KEY (funcion_id) REFERENCES funcion (id),
    CONSTRAINT fk_reserva_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (usuario_id),
    CONSTRAINT chk_reserva_expiracion CHECK (fecha_expiracion > fecha_creacion),
    CONSTRAINT chk_reserva_cantidad CHECK (cantidad IS NULL OR cantidad > 0)
);

CREATE INDEX ix_reserva_estado ON reserva (estado);

-- Tabla puente: una Reserva de asiento numerado ocupa varias Butacas de la
-- misma Funcion. Una reserva de aforo general (actividad_cultural) no tiene
-- filas aqui: usa reserva.cantidad en su lugar.
CREATE TABLE reserva_butaca (
    reserva_id  INT NOT NULL,
    butaca_id   INT NOT NULL,
    PRIMARY KEY (reserva_id, butaca_id),
    UNIQUE KEY uk_reserva_butaca_par (reserva_id, butaca_id),
    CONSTRAINT fk_reserva_butaca_reserva FOREIGN KEY (reserva_id) REFERENCES reserva (id) ON DELETE CASCADE,
    CONSTRAINT fk_reserva_butaca_butaca FOREIGN KEY (butaca_id) REFERENCES butaca (id)
);

DELIMITER $$
-- Regla de negocio central de la venta: una butaca no puede estar en dos
-- reservas ACTIVAS/CONFIRMADAS a la vez para la MISMA funcion (sigue
-- pudiendo reservarse libremente en otras funciones de la misma sala).
CREATE TRIGGER trg_reserva_butaca_sin_traslape
BEFORE INSERT ON reserva_butaca
FOR EACH ROW
BEGIN
    DECLARE v_funcion_id INT;
    DECLARE v_conflictos INT;

    SELECT funcion_id INTO v_funcion_id FROM reserva WHERE id = NEW.reserva_id;

    SELECT COUNT(*) INTO v_conflictos
    FROM reserva_butaca rb
    INNER JOIN reserva r ON r.id = rb.reserva_id
    WHERE rb.butaca_id = NEW.butaca_id
      AND r.funcion_id = v_funcion_id
      AND r.estado IN ('ACTIVA', 'CONFIRMADA')
      AND rb.reserva_id <> NEW.reserva_id;

    IF v_conflictos > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'La butaca ya esta reservada/confirmada para esta funcion';
    END IF;
END$$

-- Cada funcion vende de una sola forma (ver Funcion.requiereAsientoNumerado
-- en el modelo): una actividad_cultural es de aforo general (cantidad NOT
-- NULL, sin filas en reserva_butaca) y todo lo demas (pelicula, obra_teatro)
-- es de asiento numerado (cantidad NULL, butacas via reserva_butaca). Esta
-- misma regla ya se valida en el constructor de Reserva.java; aqui se repite
-- a nivel de base de datos para quien inserte directamente por SQL. Ademas,
-- en el caso de aforo general, valida que no se supere el aforo de la sala
-- sumando las reservas ACTIVA/CONFIRMADA existentes para esa funcion.
CREATE TRIGGER trg_reserva_tipo_y_aforo
BEFORE INSERT ON reserva
FOR EACH ROW
BEGIN
    DECLARE v_es_aforo_general INT;
    DECLARE v_aforo_sala INT;
    DECLARE v_ya_reservado INT;

    SELECT COUNT(*) INTO v_es_aforo_general
    FROM funcion f
    INNER JOIN actividad_cultural ac ON ac.evento_id = f.evento_id
    WHERE f.id = NEW.funcion_id;

    IF v_es_aforo_general > 0 THEN
        IF NEW.cantidad IS NULL THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Una reserva de actividad cultural (aforo general) requiere cantidad';
        END IF;

        SELECT s.aforo INTO v_aforo_sala
        FROM funcion f
        INNER JOIN sala s ON s.id = f.sala_id
        WHERE f.id = NEW.funcion_id;

        SELECT COALESCE(SUM(r.cantidad), 0) INTO v_ya_reservado
        FROM reserva r
        WHERE r.funcion_id = NEW.funcion_id
          AND r.estado IN ('ACTIVA', 'CONFIRMADA');

        IF v_ya_reservado + NEW.cantidad > v_aforo_sala THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'La cantidad solicitada supera el aforo disponible de la sala para esta funcion';
        END IF;
    ELSE
        IF NEW.cantidad IS NOT NULL THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Una reserva de pelicula/obra de teatro es por asiento numerado: cantidad debe quedar NULL, se reservan butacas especificas';
        END IF;
    END IF;
END$$
DELIMITER ;

-- Reserva "1" --> "1" Pago (relación uno a uno: reserva_id es UNIQUE).
CREATE TABLE pago (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    reserva_id   INT           NOT NULL,
    codigo_pago  VARCHAR(50)   NOT NULL,
    monto        DECIMAL(8,2)  NOT NULL,
    fecha_pago   DATETIME      NULL,
    estado       ENUM('PENDIENTE','VALIDADO','RECHAZADO') NOT NULL DEFAULT 'PENDIENTE',
    UNIQUE KEY uk_pago_reserva (reserva_id),
    UNIQUE KEY uk_pago_codigo (codigo_pago),
    CONSTRAINT fk_pago_reserva FOREIGN KEY (reserva_id) REFERENCES reserva (id),
    CONSTRAINT chk_pago_monto CHECK (monto >= 0)
);

-- Entrada de asiento numerado referencia el PAR (reserva_id, butaca_id) que
-- si fue reservado (FK compuesta hacia reserva_butaca, no dos FK sueltas).
-- butaca_id es NULL para una entrada de aforo general (actividad_cultural):
-- ahi la FK compuesta no aplica (MySQL/MariaDB no evalua un FK compuesto
-- cuando alguna de sus columnas es NULL), asi que se agrega ademas una FK
-- simple hacia reserva para no perder esa referencia en ese caso.
CREATE TABLE entrada (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    reserva_id     INT           NOT NULL,
    butaca_id      INT           NULL,
    fecha_emision  DATETIME      NOT NULL,
    codigo_qr      VARCHAR(100)  NOT NULL,
    precio_final   DECIMAL(8,2)  NOT NULL,
    estado         ENUM('VIGENTE','UTILIZADA','CANCELADA') NOT NULL DEFAULT 'VIGENTE',
    UNIQUE KEY uk_entrada_reserva_butaca (reserva_id, butaca_id),
    UNIQUE KEY uk_entrada_qr (codigo_qr),
    CONSTRAINT fk_entrada_reserva FOREIGN KEY (reserva_id) REFERENCES reserva (id),
    CONSTRAINT fk_entrada_reserva_butaca FOREIGN KEY (reserva_id, butaca_id)
        REFERENCES reserva_butaca (reserva_id, butaca_id),
    CONSTRAINT chk_entrada_precio CHECK (precio_final >= 0)
);

-- ---------------------------------------------------------------------
-- Paquete auditoria
-- ---------------------------------------------------------------------

-- funcion_id es opcional porque Auditoria también sustenta RNF-SEG-01
-- (trazabilidad de operaciones críticas que no siempre giran en torno a
-- una Funcion puntual, p.ej. inicios de sesión o cambios administrativos).
CREATE TABLE auditoria (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    funcion_id   INT           NULL,
    fecha        DATETIME      NOT NULL,
    responsable  VARCHAR(150)  NOT NULL,
    descripcion  VARCHAR(500)  NOT NULL,
    estado       ENUM('PENDIENTE','EN_PROCESO','RESUELTA') NOT NULL DEFAULT 'PENDIENTE',
    CONSTRAINT fk_auditoria_funcion FOREIGN KEY (funcion_id) REFERENCES funcion (id)
);

-- =====================================================================
-- Procedimientos almacenados para los DAO (patron visto en clase: cada
-- DAO llama procedimientos via CallableStatement en vez de armar SQL
-- directo). Cada integrante agrega los procedimientos de sus entidades
-- siguiendo el mismo patron de nombres (listar_x, buscar_x_por_id,
-- insertar_x, modificar_x, eliminar_x).
-- =====================================================================

DELIMITER $$

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

-- --- cliente (tabla por subtipo: usuario + cliente) -------------------
-- Cada procedimiento que escribe toca ambas tablas dentro de UNA sola
-- transaccion (START TRANSACTION/COMMIT, con ROLLBACK automatico si algo
-- falla), para que nunca quede un usuario sin su fila de cliente o
-- viceversa.

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

DELIMITER ;

-- =====================================================================
-- Datos de prueba
--
-- Cubren cada tabla y cada valor de cada enumeración. Incluyen
-- deliberadamente la MISMA película (Interestelar) programada en DOS
-- funciones distintas (funcion.id = 1 y 3, mismo evento_id = 1): el
-- catalogo (titulo, duracion, genero, clasificacion, director, precio)
-- vive una sola vez en evento/pelicula, sin repetirse por horario.
-- =====================================================================

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

INSERT INTO recurso (id, nombre, tipo, activo) VALUES
    (1, 'Proyector 4K Sala 1', 'Proyeccion', TRUE),
    (2, 'Equipo de sonido portatil', 'Audio', TRUE);

INSERT INTO bloqueo (id, motivo, fecha_inicio, fecha_fin, sala_id, recurso_id) VALUES
    (1, 'Mantenimiento preventivo de butacas', '2026-09-20 08:00:00', '2026-09-20 12:00:00', 2, NULL),
    (2, 'Calibracion de proyector', '2026-09-18 09:00:00', '2026-09-18 11:00:00', NULL, 1);

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
    (1, 1, 1, '2026-09-20', '15:00:00', '17:49:00', 'Español',     'CONFIRMADA'),
    (2, 2, 1, '2026-09-20', '20:00:00', '21:35:00', 'Subtitulada', 'PROGRAMADA'),
    (3, 1, 1, '2026-09-20', '18:00:00', '20:49:00', 'Subtitulada', 'CONFIRMADA'),
    (4, 3, 2, '2026-09-21', '19:00:00', '21:00:00', 'Español',     'PROGRAMADA'),
    (5, 4, 3, '2026-09-19', '17:00:00', '18:30:00', 'Español',     'CANCELADA'),
    (6, 5, 2, '2026-09-22', '18:00:00', '19:00:00', 'Español',     'CONFIRMADA');

-- Reservas: una confirmada (flujo completo), una pendiente de pago, una
-- expirada, y una de aforo general (funcion 6, actividad cultural: cantidad
-- en vez de butacas, sin filas en reserva_butaca).
INSERT INTO reserva (id, funcion_id, cliente_id, cantidad, fecha_creacion, fecha_expiracion, estado) VALUES
    (1, 1, 2, NULL, '2026-09-14 10:00:00', '2026-09-14 10:15:00', 'CONFIRMADA'),
    (2, 3, 3, NULL, '2026-09-14 11:00:00', '2026-09-14 11:15:00', 'ACTIVA'),
    (3, 2, 2, NULL, '2026-09-10 09:00:00', '2026-09-10 09:15:00', 'EXPIRADA'),
    (4, 6, 3, 2,    '2026-09-16 10:00:00', '2026-09-16 10:15:00', 'CONFIRMADA');

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
INSERT INTO auditoria (id, funcion_id, fecha, responsable, descripcion, estado) VALUES
    (1, 1, '2026-09-14 10:06:00', 'Marco Reyes', 'Cliente reporto butaca A2 con reposabrazos suelto durante la funcion.', 'PENDIENTE'),
    (2, NULL, '2026-09-14 09:58:00', 'valeria.suarez@gmail.com', 'Inicio de sesion exitoso desde nueva ubicacion.', 'RESUELTA');
