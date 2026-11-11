-- Reporte consolidado de funciones. No ejecuta cambios sobre la BD al ser preparado.
-- Instalar después del esquema base; MySQL 8 / MariaDB 10.11.
USE entrefunciones;

DROP PROCEDURE IF EXISTS listar_reportes_funciones;
DELIMITER $$

CREATE PROCEDURE listar_reportes_funciones(
    IN p_desde DATE,
    IN p_hasta DATE,
    IN p_sede_id INT,
    IN p_sala_id INT
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
           s.activo AS sala_activo,
           se.id AS sede_id,
           se.razon_social AS sede_razon_social,
           se.direccion AS sede_direccion,
           se.ciudad AS sede_ciudad,
           se.telefono_contacto AS sede_telefono_contacto,
           se.activo AS sede_activo,
           COALESCE(ent.entradas_no_canceladas, 0) AS entradas_no_canceladas,
           COALESCE(ent.entradas_canceladas, 0) AS entradas_canceladas,
           ent.asistentes AS asistentes,
           COALESCE(pg.ingresos_validos, 0.00) AS ingresos_validos,
           CAST(NULL AS DECIMAL(8,2)) AS porcentaje_ocupacion
    FROM funcion f
    JOIN evento e ON e.id = f.evento_id
    LEFT JOIN v_eventos v ON v.id = e.id
    JOIN sala s ON s.id = f.sala_id
    JOIN sede se ON se.id = s.sede_id
    LEFT JOIN (
        SELECT r.funcion_id,
               SUM(CASE WHEN en.estado <> 'CANCELADA' THEN 1 ELSE 0 END)
                   AS entradas_no_canceladas,
               SUM(CASE WHEN en.estado = 'CANCELADA' THEN 1 ELSE 0 END)
                   AS entradas_canceladas,
               SUM(CASE WHEN en.estado = 'UTILIZADA' THEN 1 ELSE 0 END)
                   AS asistentes
        FROM reserva r
        JOIN entrada en ON en.reserva_id = r.id
        GROUP BY r.funcion_id
    ) ent ON ent.funcion_id = f.id
    LEFT JOIN (
        SELECT r.funcion_id,
               SUM(p.monto) AS ingresos_validos
        FROM reserva r
        JOIN pago p ON p.reserva_id = r.id
        WHERE p.estado = 'VALIDADO'
        GROUP BY r.funcion_id
    ) pg ON pg.funcion_id = f.id
    WHERE f.fecha BETWEEN p_desde AND p_hasta
      AND (p_sede_id IS NULL OR s.sede_id = p_sede_id)
      AND (p_sala_id IS NULL OR f.sala_id = p_sala_id)
    ORDER BY f.fecha, f.hora_inicio, f.id;
END$$

DELIMITER ;
