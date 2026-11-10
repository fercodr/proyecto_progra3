-- Ejecutar solo sobre una copia de respaldo de la BD existente y luego instalar procedimientos.sql.
USE entrefunciones;
DROP PROCEDURE IF EXISTS migrar_lab11;
DELIMITER $$
CREATE PROCEDURE migrar_lab11()
BEGIN
    IF EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='reserva_x_butaca') THEN
        IF EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='reserva_butaca') THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Existen ambas tablas puente: conciliar sus datos antes de migrar';
        ELSE RENAME TABLE reserva_x_butaca TO reserva_butaca; END IF;
    END IF;
    IF NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='reserva' AND column_name='precio_unitario') THEN
        ALTER TABLE reserva ADD COLUMN precio_unitario DECIMAL(8,2) NULL;
        UPDATE reserva r JOIN funcion f ON f.id=r.funcion_id JOIN evento e ON e.id=f.evento_id LEFT JOIN pago p ON p.reserva_id=r.id
        SET r.precio_unitario=COALESCE(ROUND(p.monto/NULLIF(COALESCE(r.cantidad,(SELECT COUNT(*) FROM reserva_butaca rb WHERE rb.reserva_id=r.id)),0),2),e.precio);
        ALTER TABLE reserva MODIFY precio_unitario DECIMAL(8,2) NOT NULL;
    END IF;
    -- Imagen del evento: solo se agrega si la columna todavia no existe
    IF NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='evento' AND column_name='imagen_url') THEN
        ALTER TABLE evento ADD COLUMN imagen_url VARCHAR(255) NULL;
    END IF;
END$$
DELIMITER ;
CALL migrar_lab11();
DROP PROCEDURE migrar_lab11;
-- Se retiran solo las entidades descartadas por el equipo.
DROP TABLE IF EXISTS bloqueo;
DROP TABLE IF EXISTS recurso;
DROP TRIGGER IF EXISTS trg_reserva_butaca_sin_traslape;
DROP TRIGGER IF EXISTS trg_reserva_tipo_y_aforo;
DROP PROCEDURE IF EXISTS listar_recursos;
DROP PROCEDURE IF EXISTS buscar_recurso_por_id;
DROP PROCEDURE IF EXISTS insertar_recurso;
DROP PROCEDURE IF EXISTS modificar_recurso;
DROP PROCEDURE IF EXISTS eliminar_recurso;
DROP PROCEDURE IF EXISTS listar_bloqueos;
DROP PROCEDURE IF EXISTS listar_bloqueos_por_sala;
DROP PROCEDURE IF EXISTS listar_bloqueos_por_recurso;
DROP PROCEDURE IF EXISTS buscar_bloqueo_por_id;
DROP PROCEDURE IF EXISTS insertar_bloqueo;
DROP PROCEDURE IF EXISTS actualizar_bloqueo;
DROP PROCEDURE IF EXISTS eliminar_bloqueo;
DROP PROCEDURE IF EXISTS insertar_pelicula;
DROP PROCEDURE IF EXISTS modificar_pelicula;
DROP PROCEDURE IF EXISTS eliminar_pelicula;
DROP PROCEDURE IF EXISTS listar_peliculas;
DROP PROCEDURE IF EXISTS buscar_pelicula_por_id;
DROP PROCEDURE IF EXISTS insertar_obra_teatro;
DROP PROCEDURE IF EXISTS modificar_obra_teatro;
DROP PROCEDURE IF EXISTS eliminar_obra_teatro;
DROP PROCEDURE IF EXISTS listar_obras_teatro;
DROP PROCEDURE IF EXISTS buscar_obra_teatro_por_id;
DROP PROCEDURE IF EXISTS listar_butacas;
DROP PROCEDURE IF EXISTS listar_butacas_por_sala;
DROP PROCEDURE IF EXISTS buscar_butaca_por_id;
DROP PROCEDURE IF EXISTS insertar_butaca;
DROP PROCEDURE IF EXISTS modificar_butaca;
DROP PROCEDURE IF EXISTS eliminar_butaca;
DROP PROCEDURE IF EXISTS listar_butacas_por_reserva;
