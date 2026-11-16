-- LAB12 / RF-M2-01, RF-M2-02, RF-M3-01, RF-M3-02 y RF-M4-01.
-- Ejecutar solamente despues de seleccionar una base de datos de pruebas.
-- No elimina filas historicas. Los nuevos campos reciben valores compatibles
-- para que los registros preexistentes sigan siendo consultables.

ALTER TABLE sede
    ADD COLUMN IF NOT EXISTS distrito VARCHAR(100) NOT NULL DEFAULT 'Sin especificar' AFTER ciudad;
ALTER TABLE sala
    ADD COLUMN IF NOT EXISTS accesible BOOLEAN NOT NULL DEFAULT FALSE AFTER tipo_sala;
ALTER TABLE evento
    ADD COLUMN IF NOT EXISTS tematica VARCHAR(100) NULL AFTER imagen_url,
    ADD COLUMN IF NOT EXISTS publico_objetivo VARCHAR(100) NULL AFTER tematica;

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

CREATE OR REPLACE VIEW v_eventos AS
SELECT e.id,e.precio,e.activo,e.imagen_url,e.tematica,e.publico_objetivo,
 CASE WHEN p.evento_id IS NOT NULL THEN 'PELICULA'
      WHEN o.evento_id IS NOT NULL THEN 'OBRA_TEATRO'
      WHEN a.evento_id IS NOT NULL THEN 'ACTIVIDAD_CULTURAL' END AS tipo_evento,
 p.nombre_pelicula,p.duracion_minutos,p.genero,p.clasificacion_edad,p.director,
 o.titulo,o.nombre_director,o.elenco,a.nombre,a.expositor,a.tipo_actividad
FROM evento e
LEFT JOIN pelicula p ON p.evento_id=e.id
LEFT JOIN obra_teatro o ON o.evento_id=e.id
LEFT JOIN actividad_cultural a ON a.evento_id=e.id;

CREATE OR REPLACE VIEW v_funciones_auditoria AS
SELECT f.id AS funcion_id, f.evento_id, f.sala_id,
       f.fecha AS funcion_fecha, f.hora_inicio AS funcion_hora_inicio,
       f.hora_fin AS funcion_hora_fin, f.idioma AS funcion_idioma,
       f.estado AS funcion_estado,
       ev.precio AS evento_precio, ev.activo AS evento_activo,
       ev.tematica AS evento_tematica,
       ev.publico_objetivo AS evento_publico_objetivo,
       ev.tipo_evento, ev.nombre_pelicula, ev.duracion_minutos, ev.genero,
       ev.clasificacion_edad, ev.director, ev.titulo, ev.nombre_director,
       ev.elenco, ev.nombre, ev.expositor, ev.tipo_actividad,
       s.nombre AS sala_nombre, s.aforo AS sala_aforo, s.tipo_sala,
       s.accesible AS sala_accesible, s.activo AS sala_activo,
       se.id AS sede_id, se.razon_social AS sede_razon_social,
       se.direccion AS sede_direccion, se.ciudad AS sede_ciudad,
       se.distrito AS sede_distrito,
       se.telefono_contacto AS sede_telefono_contacto,
       se.activo AS sede_activo
FROM funcion f
JOIN v_eventos ev ON ev.id=f.evento_id
JOIN sala s ON s.id=f.sala_id
JOIN sede se ON se.id=s.sede_id;

DROP PROCEDURE IF EXISTS listar_sedes;
DROP PROCEDURE IF EXISTS buscar_sede_por_id;
DROP PROCEDURE IF EXISTS buscar_sede_por_razon_social;
DROP PROCEDURE IF EXISTS insertar_sede;
DROP PROCEDURE IF EXISTS modificar_sede;
DROP PROCEDURE IF EXISTS listar_salas;
DROP PROCEDURE IF EXISTS buscar_sala_por_id;
DROP PROCEDURE IF EXISTS listar_salas_por_sede;
DROP PROCEDURE IF EXISTS insertar_sala;
DROP PROCEDURE IF EXISTS modificar_sala;
DROP PROCEDURE IF EXISTS insertar_evento;
DROP PROCEDURE IF EXISTS modificar_evento;
DROP PROCEDURE IF EXISTS insertar_funcion;
DROP PROCEDURE IF EXISTS modificar_funcion;

DELIMITER $$

CREATE PROCEDURE listar_sedes()
BEGIN
    SELECT id,razon_social,direccion,ciudad,distrito,telefono_contacto,activo FROM sede;
END$$
CREATE PROCEDURE buscar_sede_por_id(IN p_id INT)
BEGIN
    SELECT id,razon_social,direccion,ciudad,distrito,telefono_contacto,activo FROM sede WHERE id=p_id;
END$$
CREATE PROCEDURE buscar_sede_por_razon_social(IN p_razon_social VARCHAR(150))
BEGIN
    SELECT id,razon_social,direccion,ciudad,distrito,telefono_contacto,activo FROM sede WHERE razon_social=p_razon_social;
END$$
CREATE PROCEDURE insertar_sede(IN p_razon_social VARCHAR(150),IN p_direccion VARCHAR(200),IN p_ciudad VARCHAR(100),IN p_distrito VARCHAR(100),IN p_telefono_contacto VARCHAR(20),IN p_activo BOOLEAN,OUT p_id INT)
BEGIN
    INSERT INTO sede(razon_social,direccion,ciudad,distrito,telefono_contacto,activo)
    VALUES(p_razon_social,p_direccion,p_ciudad,p_distrito,p_telefono_contacto,p_activo);
    SET p_id=LAST_INSERT_ID();
END$$
CREATE PROCEDURE modificar_sede(IN p_id INT,IN p_razon_social VARCHAR(150),IN p_direccion VARCHAR(200),IN p_ciudad VARCHAR(100),IN p_distrito VARCHAR(100),IN p_telefono_contacto VARCHAR(20),IN p_activo BOOLEAN)
BEGIN
    UPDATE sede SET razon_social=p_razon_social,direccion=p_direccion,ciudad=p_ciudad,
        distrito=p_distrito,telefono_contacto=p_telefono_contacto,activo=p_activo WHERE id=p_id;
END$$

CREATE PROCEDURE listar_salas()
BEGIN
    SELECT sa.id,sa.nombre,sa.aforo,sa.tipo_sala,sa.accesible,sa.activo,sa.sede_id,
           se.razon_social AS sede_razon_social,se.direccion AS sede_direccion,
           se.ciudad AS sede_ciudad,se.distrito AS sede_distrito,
           se.telefono_contacto AS sede_telefono_contacto,se.activo AS sede_activo
    FROM sala sa JOIN sede se ON se.id=sa.sede_id ORDER BY sa.id;
END$$
CREATE PROCEDURE buscar_sala_por_id(IN p_id INT)
BEGIN
    SELECT sa.id,sa.nombre,sa.aforo,sa.tipo_sala,sa.accesible,sa.activo,sa.sede_id,
           se.razon_social AS sede_razon_social,se.direccion AS sede_direccion,
           se.ciudad AS sede_ciudad,se.distrito AS sede_distrito,
           se.telefono_contacto AS sede_telefono_contacto,se.activo AS sede_activo
    FROM sala sa JOIN sede se ON se.id=sa.sede_id WHERE sa.id=p_id;
END$$
CREATE PROCEDURE listar_salas_por_sede(IN p_sede_id INT)
BEGIN
    SELECT sa.id,sa.nombre,sa.aforo,sa.tipo_sala,sa.accesible,sa.activo,sa.sede_id,
           se.razon_social AS sede_razon_social,se.direccion AS sede_direccion,
           se.ciudad AS sede_ciudad,se.distrito AS sede_distrito,
           se.telefono_contacto AS sede_telefono_contacto,se.activo AS sede_activo
    FROM sala sa JOIN sede se ON se.id=sa.sede_id WHERE sa.sede_id=p_sede_id ORDER BY sa.id;
END$$
CREATE PROCEDURE insertar_sala(IN p_sede_id INT,IN p_nombre VARCHAR(100),IN p_aforo INT,IN p_tipo_sala VARCHAR(50),IN p_accesible BOOLEAN,IN p_activo BOOLEAN,OUT p_id INT)
BEGIN
    INSERT INTO sala(sede_id,nombre,aforo,tipo_sala,accesible,activo)
    VALUES(p_sede_id,p_nombre,p_aforo,p_tipo_sala,p_accesible,p_activo);
    SET p_id=LAST_INSERT_ID();
END$$
CREATE PROCEDURE modificar_sala(IN p_id INT,IN p_sede_id INT,IN p_nombre VARCHAR(100),IN p_aforo INT,IN p_tipo_sala VARCHAR(50),IN p_accesible BOOLEAN,IN p_activo BOOLEAN)
BEGIN
    UPDATE sala SET sede_id=p_sede_id,nombre=p_nombre,aforo=p_aforo,tipo_sala=p_tipo_sala,
        accesible=p_accesible,activo=p_activo WHERE id=p_id;
END$$

CREATE PROCEDURE insertar_evento(IN p_precio DECIMAL(8,2),IN p_activo BOOLEAN,IN p_imagen_url VARCHAR(255),IN p_tematica VARCHAR(100),IN p_publico_objetivo VARCHAR(100),OUT p_id INT)
BEGIN
    INSERT INTO evento(precio,activo,imagen_url,tematica,publico_objetivo)
    VALUES(p_precio,p_activo,p_imagen_url,p_tematica,p_publico_objetivo);
    SET p_id=LAST_INSERT_ID();
END$$
CREATE PROCEDURE modificar_evento(IN p_id INT,IN p_precio DECIMAL(8,2),IN p_activo BOOLEAN,IN p_imagen_url VARCHAR(255),IN p_tematica VARCHAR(100),IN p_publico_objetivo VARCHAR(100))
BEGIN
    UPDATE evento SET precio=p_precio,activo=p_activo,imagen_url=p_imagen_url,
        tematica=p_tematica,publico_objetivo=p_publico_objetivo WHERE id=p_id;
END$$

CREATE PROCEDURE insertar_funcion(IN p_evento_id INT,IN p_sala_id INT,IN p_fecha DATE,IN p_hora_inicio TIME,IN p_hora_fin TIME,IN p_idioma VARCHAR(30),IN p_estado VARCHAR(20),OUT p_id INT)
BEGIN
    DECLARE v_activa BOOLEAN DEFAULT NULL;
    SELECT activo INTO v_activa FROM sala WHERE id=p_sala_id FOR UPDATE;
    IF v_activa IS NULL OR NOT v_activa OR NOT EXISTS(SELECT 1 FROM sala s JOIN sede se ON se.id=s.sede_id WHERE s.id=p_sala_id AND se.activo) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='La sala o sede no esta activa'; END IF;
    IF p_hora_fin<=p_hora_inicio THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Horario invalido'; END IF;
    IF p_estado='CONFIRMADA' AND EXISTS(SELECT 1 FROM funcion WHERE sala_id=p_sala_id AND fecha=p_fecha AND estado='CONFIRMADA' AND p_hora_inicio<hora_fin AND p_hora_fin>hora_inicio) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='La sala ya tiene una funcion confirmada en ese horario'; END IF;
    IF p_estado='CONFIRMADA' AND EXISTS(SELECT 1 FROM bloqueo_sala WHERE sala_id=p_sala_id AND activo=TRUE AND inicio<TIMESTAMP(p_fecha,p_hora_fin) AND fin>TIMESTAMP(p_fecha,p_hora_inicio)) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='La sala tiene un bloqueo vigente en ese horario'; END IF;
    IF NOT EXISTS(SELECT 1 FROM evento WHERE id=p_evento_id AND activo) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='El evento no esta activo'; END IF;
    INSERT INTO funcion(evento_id,sala_id,fecha,hora_inicio,hora_fin,idioma,estado)
    VALUES(p_evento_id,p_sala_id,p_fecha,p_hora_inicio,p_hora_fin,p_idioma,p_estado);
    SET p_id=LAST_INSERT_ID();
END$$

CREATE PROCEDURE modificar_funcion(IN p_id INT,IN p_sala_id INT,IN p_fecha DATE,IN p_hora_inicio TIME,IN p_hora_fin TIME,IN p_idioma VARCHAR(30),IN p_estado VARCHAR(20))
BEGIN
    DECLARE v_activa BOOLEAN DEFAULT NULL; DECLARE v_id INT;
    SELECT activo INTO v_activa FROM sala WHERE id=p_sala_id FOR UPDATE;
    IF v_activa IS NULL OR NOT v_activa OR NOT EXISTS(SELECT 1 FROM sala s JOIN sede se ON se.id=s.sede_id WHERE s.id=p_sala_id AND se.activo) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='La sala o sede no esta activa'; END IF;
    IF p_hora_fin<=p_hora_inicio THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Horario invalido'; END IF;
    IF p_estado='CONFIRMADA' AND EXISTS(SELECT 1 FROM funcion WHERE sala_id=p_sala_id AND fecha=p_fecha AND estado='CONFIRMADA' AND id<>p_id AND p_hora_inicio<hora_fin AND p_hora_fin>hora_inicio) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='La sala ya tiene una funcion confirmada en ese horario'; END IF;
    IF p_estado='CONFIRMADA' AND EXISTS(SELECT 1 FROM bloqueo_sala WHERE sala_id=p_sala_id AND activo=TRUE AND inicio<TIMESTAMP(p_fecha,p_hora_fin) AND fin>TIMESTAMP(p_fecha,p_hora_inicio)) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='La sala tiene un bloqueo vigente en ese horario'; END IF;
    SELECT id INTO v_id FROM funcion WHERE id=p_id FOR UPDATE;
    IF EXISTS(SELECT 1 FROM reserva WHERE funcion_id=p_id AND (estado='CONFIRMADA' OR (estado='ACTIVA' AND fecha_expiracion>CURRENT_TIMESTAMP))) AND EXISTS(SELECT 1 FROM funcion WHERE id=p_id AND (sala_id<>p_sala_id OR fecha<>p_fecha OR hora_inicio<>p_hora_inicio OR hora_fin<>p_hora_fin)) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='No se puede reprogramar una funcion con reservas vigentes'; END IF;
    IF p_estado='CANCELADA' THEN CALL cancelar_funcion(p_id);
    ELSE UPDATE funcion SET sala_id=p_sala_id,fecha=p_fecha,hora_inicio=p_hora_inicio,
        hora_fin=p_hora_fin,idioma=p_idioma,estado=p_estado WHERE id=p_id; END IF;
END$$

DELIMITER ;
