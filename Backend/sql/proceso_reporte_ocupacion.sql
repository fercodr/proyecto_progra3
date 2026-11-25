USE entrefunciones;

DELIMITER %%

DROP PROCEDURE IF EXISTS `sp_reporte_ocupacion_evento`%%

CREATE PROCEDURE `sp_reporte_ocupacion_evento`(
    IN p_evento_id INT
)
BEGIN
    SELECT 
        -- Datos del Evento
        e.id AS evento_id,
        COALESCE(p.nombre_pelicula, ot.titulo, ac.nombre) AS nombre_evento,
        CASE 
            WHEN p.evento_id IS NOT NULL THEN 'PELICULA'
            WHEN ot.evento_id IS NOT NULL THEN 'OBRA_TEATRO'
            WHEN ac.evento_id IS NOT NULL THEN 'ACTIVIDAD_CULTURAL'
            ELSE 'NO_DEFINIDO'
        END AS tipo_evento,

        -- Datos de la Función, Sede, Sala y Aforo
        f.id AS funcion_id,
        f.fecha AS fecha_funcion,
        f.hora_inicio,
        f.hora_fin,
        s.razon_social AS sede,
        sa.nombre AS sala,
        sa.aforo AS aforo,

        -- Datos de la Entrada
        ent.id AS entrada_id,
        CASE 
            WHEN b.id IS NOT NULL THEN CONCAT(b.fila, '-', b.columna, ' (', b.tipo_butaca, ')')
            ELSE 'Sin butaca asignada'
        END AS butaca,
        
        -- Datos del Cliente
        CONCAT(u.nombres, ' ', u.apellidos) AS cliente_nombre_completo,
        u.nombres AS cliente_nombres,
        u.apellidos AS cliente_apellidos,
        
        -- Valor y Estado
        ent.precio_final AS valor,
        ent.estado AS estado_entrada

    FROM evento e
    LEFT JOIN pelicula p ON e.id = p.evento_id
    LEFT JOIN obra_teatro ot ON e.id = ot.evento_id
    LEFT JOIN actividad_cultural ac ON e.id = ac.evento_id
    INNER JOIN funcion f ON e.id = f.evento_id
    INNER JOIN sala sa ON f.sala_id = sa.id
    INNER JOIN sede s ON sa.sede_id = s.id
    INNER JOIN reserva r ON f.id = r.funcion_id
    INNER JOIN usuario u ON r.cliente_id = u.id
    INNER JOIN entrada ent ON r.id = ent.reserva_id
    LEFT JOIN butaca b ON ent.butaca_id = b.id

    WHERE e.id = p_evento_id
      AND ent.estado IN ('VIGENTE', 'UTILIZADA')

    ORDER BY f.fecha ASC, f.hora_inicio ASC, ent.id ASC;
END%%

DELIMITER ;
