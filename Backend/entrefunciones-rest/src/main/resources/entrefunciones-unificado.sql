-- Entrefunciones: actualización consolidada e idempotente.
-- No incluye datos demo ni credenciales. Ejecutar en una copia respaldada.
CREATE DATABASE IF NOT EXISTS entrefunciones CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE entrefunciones;

-- Compatibilidad con el nombre legado de la tabla puente; conserva sus filas.
DROP PROCEDURE IF EXISTS ef_normalizar_esquema;
DELIMITER $$
CREATE PROCEDURE ef_normalizar_esquema()
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='reserva_x_butaca')
       AND NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='reserva_butaca') THEN
        RENAME TABLE reserva_x_butaca TO reserva_butaca;
    ELSEIF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='reserva_x_butaca')
       AND EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='reserva_butaca') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Existen reserva_x_butaca y reserva_butaca; concilie sus datos antes de continuar';
    END IF;
END$$
DELIMITER ;
CALL ef_normalizar_esquema();
DROP PROCEDURE ef_normalizar_esquema;

-- InstalaciÃ³n NUEVA. No elimina bases ni tablas existentes. MySQL 8 / MariaDB 10.11.
CREATE DATABASE IF NOT EXISTS entrefunciones CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE entrefunciones;

CREATE TABLE IF NOT EXISTS usuario (
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

CREATE TABLE IF NOT EXISTS administrador (
    usuario_id      INT PRIMARY KEY,
    codigo_empleado VARCHAR(20) NOT NULL,
    UNIQUE KEY uk_administrador_codigo (codigo_empleado),
    CONSTRAINT fk_administrador_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuario (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS cliente (
    usuario_id     INT PRIMARY KEY,
    fecha_registro DATE        NOT NULL,
    telefono       VARCHAR(20),
    CONSTRAINT fk_cliente_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuario (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS sede (
    id                 INT AUTO_INCREMENT PRIMARY KEY,
    razon_social       VARCHAR(150) NOT NULL,
    direccion          VARCHAR(200) NOT NULL,
    ciudad             VARCHAR(100) NOT NULL,
    distrito           VARCHAR(100) NOT NULL DEFAULT 'Sin especificar',
    telefono_contacto  VARCHAR(20),
    activo             BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS sala (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    sede_id    INT          NOT NULL,
    nombre     VARCHAR(100) NOT NULL,
    aforo      INT          NOT NULL,
    tipo_sala  VARCHAR(50)  NOT NULL,
    accesible  BOOLEAN      NOT NULL DEFAULT FALSE,
    activo     BOOLEAN      NOT NULL DEFAULT TRUE,
    UNIQUE KEY uk_sala_sede_nombre (sede_id, nombre),
    CONSTRAINT fk_sala_sede FOREIGN KEY (sede_id) REFERENCES sede (id),
    CONSTRAINT chk_sala_aforo CHECK (aforo > 0)
);

CREATE TABLE IF NOT EXISTS bloqueo_sala (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sala_id INT NOT NULL,
    inicio DATETIME NOT NULL,
    fin DATETIME NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    INDEX ix_bloqueo_sala_horario (sala_id, activo, inicio, fin),
    CONSTRAINT fk_bloqueo_sala_sala FOREIGN KEY (sala_id) REFERENCES sala(id),
    CONSTRAINT chk_bloqueo_sala_horario CHECK (fin > inicio)
);

CREATE TABLE IF NOT EXISTS butaca (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    sala_id      INT         NOT NULL,
    fila         VARCHAR(5)  NOT NULL,
    columna      INT         NOT NULL,
    tipo_butaca  VARCHAR(30) NOT NULL,
    activo       BOOLEAN     NOT NULL DEFAULT TRUE,
    UNIQUE KEY uk_butaca_sala_pos (sala_id, fila, columna),
    CONSTRAINT fk_butaca_sala FOREIGN KEY (sala_id) REFERENCES sala (id)
);

CREATE TABLE IF NOT EXISTS evento (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    precio  DECIMAL(8,2) NOT NULL,
    activo  BOOLEAN      NOT NULL DEFAULT TRUE,
    imagen_url VARCHAR(255) NULL,  -- ruta relativa, p. ej. 'img/eventos/interestelar.jpg'
    tematica VARCHAR(100) NULL,
    publico_objetivo VARCHAR(100) NULL,
    CONSTRAINT chk_evento_precio CHECK (precio >= 0)
);

CREATE TABLE IF NOT EXISTS pelicula (
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

CREATE TABLE IF NOT EXISTS obra_teatro (
    evento_id        INT PRIMARY KEY,
    titulo           VARCHAR(150) NOT NULL,
    nombre_director  VARCHAR(100),
    elenco           VARCHAR(300),
    CONSTRAINT fk_obra_teatro_evento FOREIGN KEY (evento_id)
        REFERENCES evento (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS actividad_cultural (
    evento_id       INT PRIMARY KEY,
    nombre          VARCHAR(150) NOT NULL,
    expositor       VARCHAR(100),
    tipo_actividad  ENUM('CHARLA','TALLER','EXPOSICION','CONVERSATORIO') NOT NULL,
    CONSTRAINT fk_actividad_cultural_evento FOREIGN KEY (evento_id)
        REFERENCES evento (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS funcion (
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

CREATE TABLE IF NOT EXISTS reserva (
    id                 INT AUTO_INCREMENT PRIMARY KEY,
    funcion_id         INT       NOT NULL,
    cliente_id         INT       NOT NULL,
    cantidad           INT       NULL,
    precio_unitario    DECIMAL(8,2) NOT NULL,
    fecha_creacion     DATETIME  NOT NULL,
    fecha_expiracion   DATETIME  NOT NULL,
    estado             ENUM('ACTIVA','CONFIRMADA','EXPIRADA','CANCELADA') NOT NULL DEFAULT 'ACTIVA',
    CONSTRAINT fk_reserva_funcion FOREIGN KEY (funcion_id) REFERENCES funcion (id),
    CONSTRAINT fk_reserva_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (usuario_id),
    CONSTRAINT chk_reserva_expiracion CHECK (fecha_expiracion > fecha_creacion),
    CONSTRAINT chk_reserva_cantidad CHECK (cantidad IS NULL OR cantidad > 0)
);

CREATE TABLE IF NOT EXISTS reserva_butaca (
    reserva_id  INT NOT NULL,
    butaca_id   INT NOT NULL,
    PRIMARY KEY (reserva_id, butaca_id),
    UNIQUE KEY uk_reserva_butaca_par (reserva_id, butaca_id),
    CONSTRAINT fk_reserva_butaca_reserva FOREIGN KEY (reserva_id) REFERENCES reserva (id) ON DELETE CASCADE,
    CONSTRAINT fk_reserva_butaca_butaca FOREIGN KEY (butaca_id) REFERENCES butaca (id)
);

CREATE TABLE IF NOT EXISTS pago (
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

CREATE TABLE IF NOT EXISTS entrada (
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

CREATE TABLE IF NOT EXISTS auditoria (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    funcion_id      INT           NULL,
    fecha           DATETIME      NOT NULL,
    responsable_id  INT           NULL,
    -- Respaldo temporal para filas legadas que todavia no puedan vincularse
    -- sin ambiguedad. Las altas nuevas no escriben esta columna.
    responsable     VARCHAR(150)  NULL,
    descripcion     VARCHAR(500)  NOT NULL,
    estado          ENUM('PENDIENTE','EN_PROCESO','RESUELTA') NOT NULL DEFAULT 'PENDIENTE',
    CONSTRAINT fk_auditoria_funcion FOREIGN KEY (funcion_id)
        REFERENCES funcion (id),
    CONSTRAINT fk_auditoria_responsable FOREIGN KEY (responsable_id)
        REFERENCES administrador (usuario_id) ON DELETE RESTRICT,
    CONSTRAINT chk_auditoria_responsable
        CHECK (responsable_id IS NOT NULL OR responsable IS NOT NULL)
);


-- Ajustes idempotentes de migraciones, sin borrar datos históricos.
DROP PROCEDURE IF EXISTS ef_actualizar_esquema;
DELIMITER $$
CREATE PROCEDURE ef_actualizar_esquema()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='evento' AND column_name='imagen_url') THEN
        ALTER TABLE evento ADD COLUMN imagen_url VARCHAR(255) NULL;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='reserva' AND column_name='precio_unitario') THEN
        ALTER TABLE reserva ADD COLUMN precio_unitario DECIMAL(8,2) NULL;
        UPDATE reserva r JOIN funcion f ON f.id=r.funcion_id JOIN evento e ON e.id=f.evento_id LEFT JOIN pago p ON p.reserva_id=r.id
        SET r.precio_unitario=COALESCE(ROUND(p.monto/NULLIF(COALESCE(r.cantidad,(SELECT COUNT(*) FROM reserva_butaca rb WHERE rb.reserva_id=r.id)),0),2),e.precio)
        WHERE r.precio_unitario IS NULL;
        ALTER TABLE reserva MODIFY COLUMN precio_unitario DECIMAL(8,2) NOT NULL;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='auditoria' AND column_name='responsable_id') THEN
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='auditoria' AND column_name='responsable') THEN
            ALTER TABLE auditoria ADD COLUMN responsable_id INT NULL AFTER responsable;
        ELSE
            ALTER TABLE auditoria ADD COLUMN responsable_id INT NULL;
        END IF;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='auditoria' AND column_name='responsable') THEN
        ALTER TABLE auditoria MODIFY COLUMN responsable VARCHAR(150) NULL;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='funcion' AND index_name='ix_funcion_sala_fecha') THEN CREATE INDEX ix_funcion_sala_fecha ON funcion(sala_id,fecha); END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='reserva' AND index_name='ix_reserva_funcion_estado') THEN CREATE INDEX ix_reserva_funcion_estado ON reserva(funcion_id,estado,fecha_expiracion); END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='auditoria' AND index_name='ix_auditoria_responsable') THEN CREATE INDEX ix_auditoria_responsable ON auditoria(responsable_id); END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_schema=DATABASE() AND table_name='auditoria' AND constraint_name='fk_auditoria_responsable') THEN ALTER TABLE auditoria ADD CONSTRAINT fk_auditoria_responsable FOREIGN KEY (responsable_id) REFERENCES administrador(usuario_id) ON DELETE RESTRICT; END IF;
END$$
DELIMITER ;
CALL ef_actualizar_esquema();
DROP PROCEDURE ef_actualizar_esquema;

CREATE TABLE IF NOT EXISTS auditoria_responsable_migracion_pendiente (
    auditoria_id INT PRIMARY KEY,
    responsable_texto VARCHAR(150) NOT NULL,
    coincidencias INT NOT NULL,
    motivo VARCHAR(80) NOT NULL,
    revisado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_auditoria_migracion_pendiente FOREIGN KEY (auditoria_id) REFERENCES auditoria(id) ON DELETE CASCADE
);

UPDATE auditoria au JOIN (
    SELECT c.auditoria_id,MIN(c.usuario_id) usuario_id FROM (
        SELECT a.id auditoria_id,u.id usuario_id FROM auditoria a JOIN usuario u ON LOWER(TRIM(a.responsable))=LOWER(TRIM(u.correo)) JOIN administrador ad ON ad.usuario_id=u.id
        UNION ALL SELECT a.id,u.id FROM auditoria a JOIN administrador ad ON LOWER(TRIM(a.responsable))=LOWER(TRIM(ad.codigo_empleado)) JOIN usuario u ON u.id=ad.usuario_id
        UNION ALL SELECT a.id,u.id FROM auditoria a JOIN usuario u ON LOWER(TRIM(a.responsable))=LOWER(TRIM(CONCAT(u.nombres,' ',u.apellidos))) JOIN administrador ad ON ad.usuario_id=u.id
    ) c GROUP BY c.auditoria_id HAVING COUNT(DISTINCT c.usuario_id)=1
) unicas ON unicas.auditoria_id=au.id SET au.responsable_id=unicas.usuario_id WHERE au.responsable_id IS NULL;
INSERT INTO auditoria_responsable_migracion_pendiente(auditoria_id,responsable_texto,coincidencias,motivo)
SELECT a.id,a.responsable,COUNT(DISTINCT c.usuario_id),CASE WHEN COUNT(DISTINCT c.usuario_id)=0 THEN 'SIN_COINCIDENCIA_ADMINISTRADOR' ELSE 'COINCIDENCIA_AMBIGUA' END
FROM auditoria a LEFT JOIN (
    SELECT a1.id auditoria_id,u1.id usuario_id FROM auditoria a1 JOIN usuario u1 ON LOWER(TRIM(a1.responsable))=LOWER(TRIM(u1.correo)) JOIN administrador ad1 ON ad1.usuario_id=u1.id
    UNION SELECT a2.id,u2.id FROM auditoria a2 JOIN administrador ad2 ON LOWER(TRIM(a2.responsable))=LOWER(TRIM(ad2.codigo_empleado)) JOIN usuario u2 ON u2.id=ad2.usuario_id
    UNION SELECT a3.id,u3.id FROM auditoria a3 JOIN usuario u3 ON LOWER(TRIM(a3.responsable))=LOWER(TRIM(CONCAT(u3.nombres,' ',u3.apellidos))) JOIN administrador ad3 ON ad3.usuario_id=u3.id
) c ON c.auditoria_id=a.id WHERE a.responsable_id IS NULL AND a.responsable IS NOT NULL GROUP BY a.id,a.responsable
ON DUPLICATE KEY UPDATE responsable_texto=VALUES(responsable_texto),coincidencias=VALUES(coincidencias),motivo=VALUES(motivo),revisado_en=CURRENT_TIMESTAMP;

-- Procedimientos y vistas canónicos utilizados por los DAO, autenticación, auditoría y reportes.
-- Contratos canÃ³nicos DAO. Instalar despuÃ©s del esquema o de la migraciÃ³n.
USE entrefunciones;
ALTER TABLE sede ADD COLUMN IF NOT EXISTS distrito VARCHAR(100) NOT NULL DEFAULT 'Sin especificar' AFTER ciudad;
ALTER TABLE sala ADD COLUMN IF NOT EXISTS accesible BOOLEAN NOT NULL DEFAULT FALSE AFTER tipo_sala;
ALTER TABLE evento ADD COLUMN IF NOT EXISTS tematica VARCHAR(100) NULL AFTER imagen_url;
ALTER TABLE evento ADD COLUMN IF NOT EXISTS publico_objetivo VARCHAR(100) NULL AFTER tematica;

CREATE OR REPLACE VIEW v_eventos AS
SELECT e.id,e.precio,e.activo,e.imagen_url,e.tematica,e.publico_objetivo,
 CASE WHEN p.evento_id IS NOT NULL THEN 'PELICULA' WHEN o.evento_id IS NOT NULL THEN 'OBRA_TEATRO' WHEN a.evento_id IS NOT NULL THEN 'ACTIVIDAD_CULTURAL' END AS tipo_evento,
 p.nombre_pelicula,p.duracion_minutos,p.genero,p.clasificacion_edad,p.director,
 o.titulo,o.nombre_director,o.elenco,a.nombre,a.expositor,a.tipo_actividad
FROM evento e LEFT JOIN pelicula p ON p.evento_id=e.id LEFT JOIN obra_teatro o ON o.evento_id=e.id LEFT JOIN actividad_cultural a ON a.evento_id=e.id;

CREATE OR REPLACE VIEW v_responsables_auditoria AS
SELECT u.id AS responsable_id,
       u.nombres AS responsable_nombres,
       u.apellidos AS responsable_apellidos,
       u.activo AS responsable_activo,
       ad.codigo_empleado AS responsable_codigo_empleado
FROM administrador ad
JOIN usuario u ON u.id = ad.usuario_id;

CREATE OR REPLACE VIEW v_funciones_auditoria AS
SELECT f.id AS funcion_id,
       f.evento_id,
       f.sala_id,
       f.fecha AS funcion_fecha,
       f.hora_inicio AS funcion_hora_inicio,
       f.hora_fin AS funcion_hora_fin,
       f.idioma AS funcion_idioma,
       f.estado AS funcion_estado,
       ev.precio AS evento_precio,
       ev.activo AS evento_activo,
       ev.tematica AS evento_tematica,
       ev.publico_objetivo AS evento_publico_objetivo,
       ev.tipo_evento,
       ev.nombre_pelicula,
       ev.duracion_minutos,
       ev.genero,
       ev.clasificacion_edad,
       ev.director,
       ev.titulo,
       ev.nombre_director,
       ev.elenco,
       ev.nombre,
       ev.expositor,
       ev.tipo_actividad,
       s.nombre AS sala_nombre,
       s.aforo AS sala_aforo,
       s.tipo_sala,
       s.accesible AS sala_accesible,
       s.activo AS sala_activo,
       se.id AS sede_id,
       se.razon_social AS sede_razon_social,
       se.direccion AS sede_direccion,
       se.ciudad AS sede_ciudad,
       se.distrito AS sede_distrito,
       se.telefono_contacto AS sede_telefono_contacto,
       se.activo AS sede_activo
FROM funcion f
JOIN v_eventos ev ON ev.id = f.evento_id
JOIN sala s ON s.id = f.sala_id
JOIN sede se ON se.id = s.sede_id;

CREATE OR REPLACE VIEW v_auditorias_detalle AS
SELECT a.id AS auditoria_id,
       a.fecha AS auditoria_fecha,
       a.descripcion AS auditoria_descripcion,
       a.estado AS auditoria_estado,
       a.responsable AS responsable_texto_original,
       r.responsable_id,
       r.responsable_nombres,
       r.responsable_apellidos,
       r.responsable_activo,
       r.responsable_codigo_empleado,
       f.funcion_id,
       f.evento_id,
       f.sala_id,
       f.funcion_fecha,
       f.funcion_hora_inicio,
       f.funcion_hora_fin,
       f.funcion_idioma,
       f.funcion_estado,
       f.evento_precio,
       f.evento_activo,
       f.tipo_evento,
       f.nombre_pelicula,
       f.duracion_minutos,
       f.genero,
       f.clasificacion_edad,
       f.director,
       f.titulo,
       f.nombre_director,
       f.elenco,
       f.nombre,
       f.expositor,
       f.tipo_actividad,
       f.sala_nombre,
       f.sala_aforo,
       f.tipo_sala,
       f.sala_activo,
       f.sede_id,
       f.sede_razon_social,
       f.sede_direccion,
       f.sede_ciudad,
       f.sede_telefono_contacto,
       f.sede_activo
FROM auditoria a
LEFT JOIN v_responsables_auditoria r ON r.responsable_id = a.responsable_id
LEFT JOIN v_funciones_auditoria f ON f.funcion_id = a.funcion_id;
DROP PROCEDURE IF EXISTS listar_sedes;
DROP PROCEDURE IF EXISTS buscar_sede_por_id;
DROP PROCEDURE IF EXISTS buscar_sede_por_razon_social;
DROP PROCEDURE IF EXISTS insertar_sede;
DROP PROCEDURE IF EXISTS modificar_sede;
DROP PROCEDURE IF EXISTS eliminar_sede;
DROP PROCEDURE IF EXISTS listar_salas;
DROP PROCEDURE IF EXISTS buscar_sala_por_id;
DROP PROCEDURE IF EXISTS listar_salas_por_sede;
DROP PROCEDURE IF EXISTS insertar_sala;
DROP PROCEDURE IF EXISTS modificar_sala;
DROP PROCEDURE IF EXISTS eliminar_sala;
DROP PROCEDURE IF EXISTS listar_clientes;
DROP PROCEDURE IF EXISTS buscar_cliente_por_id;
DROP PROCEDURE IF EXISTS buscar_cliente_por_correo;
DROP PROCEDURE IF EXISTS insertar_cliente;
DROP PROCEDURE IF EXISTS modificar_cliente;
DROP PROCEDURE IF EXISTS eliminar_cliente;
DROP PROCEDURE IF EXISTS buscar_cliente_por_dni;
DROP PROCEDURE IF EXISTS buscar_administrador_por_dni;
DROP PROCEDURE IF EXISTS listar_administradores;
DROP PROCEDURE IF EXISTS buscar_administrador_por_id;
DROP PROCEDURE IF EXISTS buscar_administrador_por_correo;
DROP PROCEDURE IF EXISTS buscar_administrador_por_codigo;
DROP PROCEDURE IF EXISTS insertar_administrador;
DROP PROCEDURE IF EXISTS modificar_administrador;
DROP PROCEDURE IF EXISTS eliminar_administrador;
DROP PROCEDURE IF EXISTS listar_eventos;
DROP PROCEDURE IF EXISTS buscar_evento_por_id;
DROP PROCEDURE IF EXISTS insertar_evento;
DROP PROCEDURE IF EXISTS modificar_evento;
DROP PROCEDURE IF EXISTS eliminar_evento;
DROP PROCEDURE IF EXISTS insertar_evento_pelicula;
DROP PROCEDURE IF EXISTS modificar_evento_pelicula;
DROP PROCEDURE IF EXISTS insertar_evento_obra_teatro;
DROP PROCEDURE IF EXISTS modificar_evento_obra_teatro;
DROP PROCEDURE IF EXISTS insertar_evento_actividad_cultural;
DROP PROCEDURE IF EXISTS modificar_evento_actividad_cultural;
DROP PROCEDURE IF EXISTS listar_funciones;
DROP PROCEDURE IF EXISTS buscar_funcion_por_id;
DROP PROCEDURE IF EXISTS listar_funciones_por_evento;
DROP PROCEDURE IF EXISTS listar_funciones_por_sala;
DROP PROCEDURE IF EXISTS listar_reportes_funciones;
DROP PROCEDURE IF EXISTS insertar_funcion;
DROP PROCEDURE IF EXISTS modificar_funcion;
DROP PROCEDURE IF EXISTS cancelar_funcion;
DROP PROCEDURE IF EXISTS cancelar_funciones_por_evento;
DROP PROCEDURE IF EXISTS listar_auditorias;
DROP PROCEDURE IF EXISTS buscar_auditoria_por_id;
DROP PROCEDURE IF EXISTS listar_auditorias_filtradas;
DROP PROCEDURE IF EXISTS listar_responsables_activos_auditoria;
DROP PROCEDURE IF EXISTS listar_funciones_asignables_auditoria;
DROP PROCEDURE IF EXISTS buscar_responsable_auditoria_por_id;
DROP PROCEDURE IF EXISTS buscar_funcion_auditoria_por_id;
DROP PROCEDURE IF EXISTS insertar_auditoria;
DROP PROCEDURE IF EXISTS modificar_auditoria_atencion;
DROP PROCEDURE IF EXISTS listar_reservas;
DROP PROCEDURE IF EXISTS buscar_reserva_por_id;
DROP PROCEDURE IF EXISTS listar_reservas_por_cliente;
DROP PROCEDURE IF EXISTS listar_reservas_por_funcion;
DROP PROCEDURE IF EXISTS insertar_reserva;
DROP PROCEDURE IF EXISTS insertar_reserva_butaca;
DROP PROCEDURE IF EXISTS modificar_reserva;
DROP PROCEDURE IF EXISTS eliminar_reserva;
DROP PROCEDURE IF EXISTS listar_pagos;
DROP PROCEDURE IF EXISTS buscar_pago_por_id;
DROP PROCEDURE IF EXISTS buscar_pago_por_reserva;
DROP PROCEDURE IF EXISTS buscar_pago_por_codigo;
DROP PROCEDURE IF EXISTS insertar_pago;
DROP PROCEDURE IF EXISTS modificar_pago;
DROP PROCEDURE IF EXISTS eliminar_pago;
DROP PROCEDURE IF EXISTS listar_entradas;
DROP PROCEDURE IF EXISTS buscar_entrada_por_id;
DROP PROCEDURE IF EXISTS buscar_entrada_por_reserva;
DROP PROCEDURE IF EXISTS buscar_entrada_por_qr;
DROP PROCEDURE IF EXISTS insertar_entrada;
DROP PROCEDURE IF EXISTS modificar_entrada;
DROP PROCEDURE IF EXISTS eliminar_entrada;
DELIMITER $$

CREATE PROCEDURE listar_sedes()
BEGIN
    SELECT id, razon_social, direccion, ciudad, distrito, telefono_contacto, activo
    FROM sede;
END$$

CREATE PROCEDURE buscar_sede_por_id(IN p_id INT)
BEGIN
    SELECT id, razon_social, direccion, ciudad, distrito, telefono_contacto, activo
    FROM sede
    WHERE id = p_id;
END$$

CREATE PROCEDURE buscar_sede_por_razon_social(IN p_razon_social VARCHAR(150))
BEGIN
    SELECT id, razon_social, direccion, ciudad, distrito, telefono_contacto, activo
    FROM sede
    WHERE razon_social = p_razon_social;
END$$

CREATE PROCEDURE insertar_sede(
    IN p_razon_social VARCHAR(150),
    IN p_direccion VARCHAR(200),
    IN p_ciudad VARCHAR(80),
    IN p_distrito VARCHAR(100),
    IN p_telefono_contacto VARCHAR(20),
    IN p_activo BOOLEAN,
    OUT p_id INT
)
BEGIN
    INSERT INTO sede (razon_social, direccion, ciudad, distrito, telefono_contacto, activo)
    VALUES (p_razon_social, p_direccion, p_ciudad, p_distrito, p_telefono_contacto, p_activo);
    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_sede(
    IN p_id INT,
    IN p_razon_social VARCHAR(150),
    IN p_direccion VARCHAR(200),
    IN p_ciudad VARCHAR(80),
    IN p_distrito VARCHAR(100),
    IN p_telefono_contacto VARCHAR(20),
    IN p_activo BOOLEAN
)
BEGIN
    UPDATE sede
    SET razon_social = p_razon_social,
        direccion = p_direccion,
        ciudad = p_ciudad,
        distrito = p_distrito,
        telefono_contacto = p_telefono_contacto,
        activo = p_activo
    WHERE id = p_id;
END$$

CREATE PROCEDURE eliminar_sede(IN p_id INT)
BEGIN
    -- Eliminado logico: nunca se borra la fila, solo se desactiva.
    UPDATE sede SET activo = FALSE WHERE id = p_id;
END$$

CREATE PROCEDURE listar_salas()
BEGIN
    SELECT sa.id, sa.nombre, sa.aforo, sa.tipo_sala, sa.accesible, sa.activo,
           sa.sede_id,
           se.razon_social AS sede_razon_social,
           se.direccion AS sede_direccion,
           se.ciudad AS sede_ciudad,
           se.distrito AS sede_distrito,
           se.telefono_contacto AS sede_telefono_contacto,
           se.activo AS sede_activo
    FROM sala sa
    INNER JOIN sede se ON se.id = sa.sede_id
    ORDER BY sa.id;
END$$

CREATE PROCEDURE buscar_sala_por_id(IN p_id INT)
BEGIN
    SELECT sa.id, sa.nombre, sa.aforo, sa.tipo_sala, sa.accesible, sa.activo,
           sa.sede_id,
           se.razon_social AS sede_razon_social,
           se.direccion AS sede_direccion,
           se.ciudad AS sede_ciudad,
           se.distrito AS sede_distrito,
           se.telefono_contacto AS sede_telefono_contacto,
           se.activo AS sede_activo
    FROM sala sa
    INNER JOIN sede se ON se.id = sa.sede_id
    WHERE sa.id = p_id;
END$$

CREATE PROCEDURE listar_salas_por_sede(IN p_sede_id INT)
BEGIN
    SELECT sa.id, sa.nombre, sa.aforo, sa.tipo_sala, sa.accesible, sa.activo,
           sa.sede_id,
           se.razon_social AS sede_razon_social,
           se.direccion AS sede_direccion,
           se.ciudad AS sede_ciudad,
           se.distrito AS sede_distrito,
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
    IN p_accesible BOOLEAN,
    IN p_activo BOOLEAN,
    OUT p_id INT
)
BEGIN
    INSERT INTO sala (sede_id, nombre, aforo, tipo_sala, accesible, activo)
    VALUES (p_sede_id, p_nombre, p_aforo, p_tipo_sala, p_accesible, p_activo);
    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_sala(
    IN p_id INT,
    IN p_sede_id INT,
    IN p_nombre VARCHAR(100),
    IN p_aforo INT,
    IN p_tipo_sala VARCHAR(50),
    IN p_accesible BOOLEAN,
    IN p_activo BOOLEAN
)
BEGIN
    UPDATE sala
    SET sede_id = p_sede_id,
        nombre = p_nombre,
        aforo = p_aforo,
        tipo_sala = p_tipo_sala,
        accesible = p_accesible,
        activo = p_activo
    WHERE id = p_id;
END$$

CREATE PROCEDURE eliminar_sala(IN p_id INT)
BEGIN
    UPDATE sala SET activo = FALSE WHERE id = p_id;
END$$

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

CREATE PROCEDURE buscar_cliente_por_dni(IN p_dni VARCHAR(15))
BEGIN
SELECT u.*, c.fecha_registro,c.telefono FROM usuario u JOIN cliente c ON c.usuario_id=u.id WHERE u.dni=p_dni;
END$$

CREATE PROCEDURE buscar_administrador_por_dni(IN p_dni VARCHAR(15))
BEGIN
SELECT u.*, c.codigo_empleado FROM usuario u JOIN administrador c ON c.usuario_id=u.id WHERE u.dni=p_dni;
END$$

CREATE PROCEDURE listar_administradores()
BEGIN
SELECT u.*, c.codigo_empleado FROM usuario u JOIN administrador c ON c.usuario_id=u.id;
END$$

CREATE PROCEDURE buscar_administrador_por_id(IN p_id INT)
BEGIN
SELECT u.*, c.codigo_empleado FROM usuario u JOIN administrador c ON c.usuario_id=u.id WHERE u.id=p_id;
END$$

CREATE PROCEDURE buscar_administrador_por_correo(IN p_correo VARCHAR(150))
BEGIN
SELECT u.*, c.codigo_empleado FROM usuario u JOIN administrador c ON c.usuario_id=u.id WHERE u.correo=p_correo;
END$$

CREATE PROCEDURE buscar_administrador_por_codigo(IN p_codigo_empleado VARCHAR(20))
BEGIN
SELECT u.*, c.codigo_empleado FROM usuario u JOIN administrador c ON c.usuario_id=u.id WHERE c.codigo_empleado=p_codigo_empleado;
END$$

CREATE PROCEDURE insertar_administrador(IN p_nombres VARCHAR(100),IN p_apellidos VARCHAR(100),IN p_correo VARCHAR(150),IN p_contrasena VARCHAR(255),IN p_dni VARCHAR(15),IN p_activo BOOLEAN,IN p_codigo_empleado VARCHAR(20),OUT p_id INT)
BEGIN
DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
START TRANSACTION;
INSERT INTO usuario(nombres,apellidos,correo,contrasena,dni,activo) VALUES(p_nombres,p_apellidos,p_correo,p_contrasena,p_dni,p_activo);
SET p_id=LAST_INSERT_ID();
INSERT INTO administrador(usuario_id,codigo_empleado) VALUES(p_id,p_codigo_empleado);
COMMIT;
END$$

CREATE PROCEDURE modificar_administrador(IN p_nombres VARCHAR(100),IN p_apellidos VARCHAR(100),IN p_correo VARCHAR(150),IN p_contrasena VARCHAR(255),IN p_dni VARCHAR(15),IN p_activo BOOLEAN,IN p_codigo_empleado VARCHAR(20),IN p_id INT)
BEGIN
DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
START TRANSACTION;
UPDATE usuario SET nombres=p_nombres,apellidos=p_apellidos,correo=p_correo,contrasena=p_contrasena,dni=p_dni,activo=p_activo WHERE id=p_id;
UPDATE administrador SET codigo_empleado=p_codigo_empleado WHERE usuario_id=p_id;
COMMIT;
END$$

CREATE PROCEDURE eliminar_administrador(IN p_id INT)
BEGIN
UPDATE usuario SET activo=FALSE WHERE id=p_id;
END$$

CREATE PROCEDURE listar_eventos()
BEGIN
SELECT * FROM v_eventos;
END$$

CREATE PROCEDURE buscar_evento_por_id(IN p_id INT)
BEGIN
SELECT * FROM v_eventos WHERE id=p_id;
END$$

CREATE PROCEDURE insertar_evento(IN p_precio DECIMAL(8,2),IN p_activo BOOLEAN,IN p_imagen_url VARCHAR(255),IN p_tematica VARCHAR(100),IN p_publico_objetivo VARCHAR(100),OUT p_id INT)
BEGIN
INSERT INTO evento(precio,activo,imagen_url,tematica,publico_objetivo) VALUES(p_precio,p_activo,p_imagen_url,p_tematica,p_publico_objetivo); SET p_id=LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_evento(IN p_id INT,IN p_precio DECIMAL(8,2),IN p_activo BOOLEAN,IN p_imagen_url VARCHAR(255),IN p_tematica VARCHAR(100),IN p_publico_objetivo VARCHAR(100))
BEGIN
UPDATE evento SET precio=p_precio,activo=p_activo,imagen_url=p_imagen_url,tematica=p_tematica,publico_objetivo=p_publico_objetivo WHERE id=p_id;
END$$

CREATE PROCEDURE eliminar_evento(IN p_id INT)
BEGIN
UPDATE evento SET activo=FALSE WHERE id=p_id;
END$$

CREATE PROCEDURE insertar_evento_pelicula(IN p_evento_id INT,IN p_nombre_pelicula VARCHAR(150),IN p_duracion_minutos INT,IN p_genero VARCHAR(30),IN p_clasificacion_edad VARCHAR(30),IN p_director VARCHAR(100))
BEGIN
INSERT INTO pelicula(evento_id,nombre_pelicula,duracion_minutos,genero,clasificacion_edad,director) VALUES(p_evento_id,p_nombre_pelicula,p_duracion_minutos,p_genero,p_clasificacion_edad,p_director);
END$$

CREATE PROCEDURE modificar_evento_pelicula(IN p_evento_id INT,IN p_nombre_pelicula VARCHAR(150),IN p_duracion_minutos INT,IN p_genero VARCHAR(30),IN p_clasificacion_edad VARCHAR(30),IN p_director VARCHAR(100))
BEGIN
UPDATE pelicula SET nombre_pelicula=p_nombre_pelicula,duracion_minutos=p_duracion_minutos,genero=p_genero,clasificacion_edad=p_clasificacion_edad,director=p_director WHERE evento_id=p_evento_id;
END$$

CREATE PROCEDURE insertar_evento_obra_teatro(IN p_evento_id INT,IN p_titulo VARCHAR(150),IN p_nombre_director VARCHAR(100),IN p_elenco VARCHAR(300))
BEGIN
INSERT INTO obra_teatro(evento_id,titulo,nombre_director,elenco) VALUES(p_evento_id,p_titulo,p_nombre_director,p_elenco);
END$$

CREATE PROCEDURE modificar_evento_obra_teatro(IN p_evento_id INT,IN p_titulo VARCHAR(150),IN p_nombre_director VARCHAR(100),IN p_elenco VARCHAR(300))
BEGIN
UPDATE obra_teatro SET titulo=p_titulo,nombre_director=p_nombre_director,elenco=p_elenco WHERE evento_id=p_evento_id;
END$$

CREATE PROCEDURE insertar_evento_actividad_cultural(IN p_evento_id INT,IN p_nombre VARCHAR(150),IN p_expositor VARCHAR(100),IN p_tipo_actividad VARCHAR(30))
BEGIN
INSERT INTO actividad_cultural(evento_id,nombre,expositor,tipo_actividad) VALUES(p_evento_id,p_nombre,p_expositor,p_tipo_actividad);
END$$

CREATE PROCEDURE modificar_evento_actividad_cultural(IN p_evento_id INT,IN p_nombre VARCHAR(150),IN p_expositor VARCHAR(100),IN p_tipo_actividad VARCHAR(30))
BEGIN
UPDATE actividad_cultural SET nombre=p_nombre,expositor=p_expositor,tipo_actividad=p_tipo_actividad WHERE evento_id=p_evento_id;
END$$

CREATE PROCEDURE listar_funciones()
BEGIN
SELECT * FROM funcion;
END$$

CREATE PROCEDURE buscar_funcion_por_id(IN p_id INT)
BEGIN
SELECT * FROM funcion WHERE id=p_id;
END$$

CREATE PROCEDURE listar_funciones_por_evento(IN p_evento_id INT)
BEGIN
SELECT * FROM funcion WHERE evento_id=p_evento_id;
END$$

CREATE PROCEDURE listar_funciones_por_sala(IN p_sala_id INT)
BEGIN
SELECT * FROM funcion WHERE sala_id=p_sala_id;
END$$

CREATE PROCEDURE listar_reportes_funciones(
    IN p_desde DATE,
    IN p_hasta DATE,
    IN p_sede_id INT,
    IN p_sala_id INT,
    IN p_tipo_evento VARCHAR(30)
)
BEGIN
    SELECT f.id AS funcion_id,
           f.evento_id,
           f.fecha AS funcion_fecha,
           f.hora_inicio AS funcion_hora_inicio,
           f.hora_fin AS funcion_hora_fin,
           f.idioma AS funcion_idioma,
           f.estado AS funcion_estado,
           e.precio AS evento_precio,
           e.activo AS evento_activo,
           v.tematica AS evento_tematica,
           v.publico_objetivo AS evento_publico_objetivo,
           v.tipo_evento,
           v.nombre_pelicula,
           v.duracion_minutos,
           v.genero,
           v.clasificacion_edad,
           v.director,
           v.titulo,
           v.nombre_director,
           v.elenco,
           v.nombre,
           v.expositor,
           v.tipo_actividad,
           s.id AS sala_id,
           s.nombre AS sala_nombre,
           s.aforo AS sala_aforo,
           s.tipo_sala AS sala_tipo_sala,
           s.accesible AS sala_accesible,
           s.activo AS sala_activo,
           se.id AS sede_id,
           se.razon_social AS sede_razon_social,
           se.direccion AS sede_direccion,
           se.ciudad AS sede_ciudad,
           se.distrito AS sede_distrito,
           se.telefono_contacto AS sede_telefono_contacto,
           se.activo AS sede_activo,
           COALESCE(ent.entradas_vigentes, 0) AS entradas_vigentes,
           COALESCE(ent.entradas_emitidas, 0) AS entradas_emitidas,
           COALESCE(ent.entradas_canceladas, 0) AS entradas_canceladas,
           COALESCE(pg.ingresos_confirmados, 0.00) AS ingresos_confirmados,
           CAST(NULL AS DECIMAL(8,2)) AS porcentaje_ocupacion
    FROM funcion f
    JOIN evento e ON e.id = f.evento_id
    LEFT JOIN v_eventos v ON v.id = e.id
    JOIN sala s ON s.id = f.sala_id
    JOIN sede se ON se.id = s.sede_id
    LEFT JOIN (
        SELECT r.funcion_id,
               SUM(CASE WHEN en.estado = 'VIGENTE' THEN 1 ELSE 0 END)
                   AS entradas_vigentes,
               COUNT(en.id) AS entradas_emitidas,
               SUM(CASE WHEN en.estado = 'CANCELADA' THEN 1 ELSE 0 END)
                   AS entradas_canceladas
        FROM reserva r
        JOIN entrada en ON en.reserva_id = r.id
        GROUP BY r.funcion_id
    ) ent ON ent.funcion_id = f.id
    LEFT JOIN (
        SELECT r.funcion_id,
               SUM(p.monto) AS ingresos_confirmados
        FROM reserva r
        JOIN pago p ON p.reserva_id = r.id
        WHERE p.estado = 'VALIDADO'
        GROUP BY r.funcion_id
    ) pg ON pg.funcion_id = f.id
    WHERE (p_desde IS NULL OR f.fecha >= p_desde)
      AND (p_hasta IS NULL OR f.fecha <= p_hasta)
      AND (p_sede_id IS NULL OR s.sede_id = p_sede_id)
      AND (p_sala_id IS NULL OR f.sala_id = p_sala_id)
      AND (p_tipo_evento IS NULL OR v.tipo_evento = p_tipo_evento)
    ORDER BY f.fecha, f.hora_inicio, f.id;
END$$

CREATE PROCEDURE insertar_funcion(IN p_evento_id INT,IN p_sala_id INT,IN p_fecha DATE,IN p_hora_inicio TIME,IN p_hora_fin TIME,IN p_idioma VARCHAR(30),IN p_estado VARCHAR(20),OUT p_id INT)
BEGIN
DECLARE v_activa BOOLEAN DEFAULT NULL; DECLARE v_excluir INT DEFAULT 0;
SELECT activo INTO v_activa FROM sala WHERE id=p_sala_id FOR UPDATE;
IF v_activa IS NULL OR NOT v_activa OR NOT EXISTS(SELECT 1 FROM sala s JOIN sede se ON se.id=s.sede_id WHERE s.id=p_sala_id AND se.activo) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='La sala o sede no esta activa'; END IF;
IF p_hora_fin<=p_hora_inicio THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='El horario debe terminar en el mismo dia despues del inicio'; END IF;
IF p_estado='CONFIRMADA' AND EXISTS(SELECT 1 FROM funcion WHERE sala_id=p_sala_id AND fecha=p_fecha AND estado='CONFIRMADA' AND id<>v_excluir AND p_hora_inicio<hora_fin AND p_hora_fin>hora_inicio) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='La sala ya tiene una funcion confirmada en ese horario'; END IF;
IF p_estado='CONFIRMADA' AND EXISTS(SELECT 1 FROM bloqueo_sala WHERE sala_id=p_sala_id AND activo=TRUE AND inicio<TIMESTAMP(p_fecha,p_hora_fin) AND fin>TIMESTAMP(p_fecha,p_hora_inicio)) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='La sala tiene un bloqueo vigente en ese horario'; END IF;
IF NOT EXISTS(SELECT 1 FROM evento WHERE id=p_evento_id AND activo) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='El evento no esta activo'; END IF;
INSERT INTO funcion(evento_id,sala_id,fecha,hora_inicio,hora_fin,idioma,estado) VALUES(p_evento_id,p_sala_id,p_fecha,p_hora_inicio,p_hora_fin,p_idioma,p_estado); SET p_id=LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_funcion(IN p_id INT,IN p_sala_id INT,IN p_fecha DATE,IN p_hora_inicio TIME,IN p_hora_fin TIME,IN p_idioma VARCHAR(30),IN p_estado VARCHAR(20))
BEGIN
DECLARE v_activa BOOLEAN DEFAULT NULL; DECLARE v_excluir INT; DECLARE v_id INT;
SET v_excluir=p_id;
SELECT activo INTO v_activa FROM sala WHERE id=p_sala_id FOR UPDATE;
IF v_activa IS NULL OR NOT v_activa OR NOT EXISTS(SELECT 1 FROM sala s JOIN sede se ON se.id=s.sede_id WHERE s.id=p_sala_id AND se.activo) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='La sala o sede no esta activa'; END IF;
IF p_hora_fin<=p_hora_inicio THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='El horario debe terminar en el mismo dia despues del inicio'; END IF;
IF p_estado='CONFIRMADA' AND EXISTS(SELECT 1 FROM funcion WHERE sala_id=p_sala_id AND fecha=p_fecha AND estado='CONFIRMADA' AND id<>v_excluir AND p_hora_inicio<hora_fin AND p_hora_fin>hora_inicio) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='La sala ya tiene una funcion confirmada en ese horario'; END IF;
IF p_estado='CONFIRMADA' AND EXISTS(SELECT 1 FROM bloqueo_sala WHERE sala_id=p_sala_id AND activo=TRUE AND inicio<TIMESTAMP(p_fecha,p_hora_fin) AND fin>TIMESTAMP(p_fecha,p_hora_inicio)) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='La sala tiene un bloqueo vigente en ese horario'; END IF;
SELECT id INTO v_id FROM funcion WHERE id=p_id FOR UPDATE;
IF EXISTS(SELECT 1 FROM reserva WHERE funcion_id=p_id AND (estado='CONFIRMADA' OR (estado='ACTIVA' AND fecha_expiracion>CURRENT_TIMESTAMP))) AND EXISTS(SELECT 1 FROM funcion WHERE id=p_id AND (sala_id<>p_sala_id OR fecha<>p_fecha OR hora_inicio<>p_hora_inicio OR hora_fin<>p_hora_fin)) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='No se puede reprogramar una funcion con reservas vigentes'; END IF;
IF p_estado='CANCELADA' THEN CALL cancelar_funcion(p_id);
ELSE UPDATE funcion SET sala_id=p_sala_id,fecha=p_fecha,hora_inicio=p_hora_inicio,hora_fin=p_hora_fin,idioma=p_idioma,estado=p_estado WHERE id=p_id; END IF;
END$$

CREATE PROCEDURE cancelar_funcion(IN p_id INT)
BEGIN
DECLARE v_id INT;
SELECT id INTO v_id FROM funcion WHERE id=p_id FOR UPDATE;
UPDATE funcion SET estado='CANCELADA' WHERE id=p_id;
UPDATE reserva SET estado='CANCELADA' WHERE funcion_id=p_id AND estado='ACTIVA';
UPDATE pago p JOIN reserva r ON r.id=p.reserva_id SET p.estado='RECHAZADO' WHERE r.funcion_id=p_id AND p.estado='PENDIENTE';
UPDATE entrada e JOIN reserva r ON r.id=e.reserva_id SET e.estado='CANCELADA' WHERE r.funcion_id=p_id AND e.estado='VIGENTE';
END$$

CREATE PROCEDURE cancelar_funciones_por_evento(IN p_evento_id INT)
BEGIN
DECLARE v_fin BOOLEAN DEFAULT FALSE; DECLARE v_id INT;
DECLARE c CURSOR FOR SELECT id FROM funcion WHERE evento_id=p_evento_id ORDER BY id;
DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_fin=TRUE;
OPEN c;
bucle: LOOP FETCH c INTO v_id; IF v_fin THEN LEAVE bucle; END IF; CALL cancelar_funcion(v_id); END LOOP;
CLOSE c;
END$$

CREATE PROCEDURE listar_auditorias()
BEGIN
    SELECT *
    FROM v_auditorias_detalle
    ORDER BY auditoria_fecha DESC, auditoria_id DESC;
END$$

CREATE PROCEDURE buscar_auditoria_por_id(IN p_id INT)
BEGIN
    SELECT *
    FROM v_auditorias_detalle
    WHERE auditoria_id = p_id;
END$$

CREATE PROCEDURE listar_auditorias_filtradas(
    IN p_descripcion VARCHAR(500),
    IN p_estado VARCHAR(20)
)
BEGIN
    SELECT *
    FROM v_auditorias_detalle
    WHERE (p_descripcion IS NULL OR TRIM(p_descripcion) = ''
           OR auditoria_descripcion LIKE CONCAT('%', TRIM(p_descripcion), '%'))
      AND (p_estado IS NULL OR TRIM(p_estado) = '' OR auditoria_estado = p_estado)
    ORDER BY auditoria_fecha DESC, auditoria_id DESC;
END$$

CREATE PROCEDURE listar_responsables_activos_auditoria()
BEGIN
    SELECT *
    FROM v_responsables_auditoria
    WHERE responsable_activo = TRUE
    ORDER BY responsable_apellidos, responsable_nombres, responsable_id;
END$$

CREATE PROCEDURE listar_funciones_asignables_auditoria()
BEGIN
    SELECT *
    FROM v_funciones_auditoria
    WHERE funcion_estado <> 'CANCELADA'
      AND evento_activo = TRUE
      AND sala_activo = TRUE
      AND sede_activo = TRUE
    ORDER BY funcion_fecha DESC, funcion_hora_inicio, funcion_id;
END$$

CREATE PROCEDURE buscar_responsable_auditoria_por_id(IN p_usuario_id INT)
BEGIN
    SELECT *
    FROM v_responsables_auditoria
    WHERE responsable_id = p_usuario_id;
END$$

CREATE PROCEDURE buscar_funcion_auditoria_por_id(IN p_funcion_id INT)
BEGIN
    SELECT *
    FROM v_funciones_auditoria
    WHERE funcion_id = p_funcion_id;
END$$

CREATE PROCEDURE insertar_auditoria(
    IN p_funcion_id INT,
    IN p_responsable_id INT,
    IN p_descripcion VARCHAR(500),
    OUT p_id INT,
    OUT p_fecha DATETIME
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM administrador ad
        JOIN usuario u ON u.id = ad.usuario_id
        WHERE ad.usuario_id = p_responsable_id AND u.activo = TRUE
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'El responsable no es un administrador activo';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM funcion f
        JOIN evento ev ON ev.id = f.evento_id
        JOIN sala s ON s.id = f.sala_id
        JOIN sede se ON se.id = s.sede_id
        WHERE f.id = p_funcion_id
          AND f.estado <> 'CANCELADA'
          AND ev.activo = TRUE
          AND s.activo = TRUE
          AND se.activo = TRUE
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'La funcion no esta disponible para una incidencia nueva';
    END IF;

    SET p_fecha = CURRENT_TIMESTAMP;
    INSERT INTO auditoria
        (funcion_id, fecha, responsable_id, responsable, descripcion, estado)
    VALUES
        (p_funcion_id, p_fecha, p_responsable_id, NULL, p_descripcion, 'PENDIENTE');
    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_auditoria_atencion(
    IN p_id INT,
    IN p_descripcion VARCHAR(500),
    IN p_responsable_id INT,
    IN p_estado VARCHAR(20)
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM auditoria WHERE id = p_id) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'La auditoria indicada no existe';
    END IF;

    IF p_descripcion IS NULL OR CHAR_LENGTH(TRIM(p_descripcion)) = 0
       OR CHAR_LENGTH(TRIM(p_descripcion)) > 500 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'La descripcion debe contener entre 1 y 500 caracteres';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM administrador ad
        JOIN usuario u ON u.id = ad.usuario_id
        WHERE ad.usuario_id = p_responsable_id AND u.activo = TRUE
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'El responsable no es un administrador activo';
    END IF;

    IF p_estado IS NULL
       OR p_estado NOT IN ('PENDIENTE', 'EN_PROCESO', 'RESUELTA') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'El estado de auditoria no es valido';
    END IF;

    -- Se conserva el registro, la fecha y la funcion historica.
    UPDATE auditoria
    SET descripcion = TRIM(p_descripcion),
        responsable_id = p_responsable_id,
        estado = p_estado
    WHERE id = p_id;
END$$

CREATE PROCEDURE listar_reservas()
BEGIN
SELECT * FROM reserva;
END$$

CREATE PROCEDURE buscar_reserva_por_id(IN p_id INT)
BEGIN
SELECT * FROM reserva WHERE id=p_id;
END$$

CREATE PROCEDURE listar_reservas_por_cliente(IN p_cliente_id INT)
BEGIN
SELECT * FROM reserva WHERE cliente_id=p_cliente_id;
END$$

CREATE PROCEDURE listar_reservas_por_funcion(IN p_funcion_id INT)
BEGIN
SELECT * FROM reserva WHERE funcion_id=p_funcion_id;
END$$

CREATE PROCEDURE insertar_reserva(IN p_funcion_id INT,IN p_cliente_id INT,IN p_fecha_creacion DATETIME,IN p_fecha_expiracion DATETIME,IN p_estado VARCHAR(20),IN p_cantidad INT,IN p_precio_unitario DECIMAL(8,2),OUT p_id INT)
BEGIN
INSERT INTO reserva(funcion_id,cliente_id,fecha_creacion,fecha_expiracion,estado,cantidad,precio_unitario) VALUES(p_funcion_id,p_cliente_id,p_fecha_creacion,p_fecha_expiracion,p_estado,p_cantidad,p_precio_unitario); SET p_id=LAST_INSERT_ID();
END$$

CREATE PROCEDURE insertar_reserva_butaca(IN p_reserva_id INT,IN p_butaca_id INT)
BEGIN
INSERT INTO reserva_butaca(reserva_id,butaca_id) VALUES(p_reserva_id,p_butaca_id);
END$$

CREATE PROCEDURE modificar_reserva(IN p_id INT,IN p_estado VARCHAR(20))
BEGIN
UPDATE reserva SET estado=p_estado WHERE id=p_id;
END$$

CREATE PROCEDURE eliminar_reserva(IN p_id INT)
BEGIN
UPDATE reserva SET estado='CANCELADA' WHERE id=p_id AND estado='ACTIVA';
END$$

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
