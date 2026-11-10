-- Migracion revisable: auditoria.responsable (texto) -> administrador.usuario_id.
--
-- Ejecutar primero en una copia aislada. Este script NO elimina la columna
-- responsable ni obliga responsable_id a NOT NULL: ambas acciones se posponen
-- hasta que el reporte final no tenga filas pendientes.
USE entrefunciones;

ALTER TABLE auditoria
    ADD COLUMN responsable_id INT NULL AFTER responsable;

-- La columna se conserva como respaldo, pero deja de ser obligatoria para las
-- altas nuevas que ya escriben exclusivamente responsable_id.
ALTER TABLE auditoria
    MODIFY COLUMN responsable VARCHAR(150) NULL;

CREATE INDEX ix_auditoria_responsable ON auditoria(responsable_id);

-- Una coincidencia es segura solo cuando el texto completo coincide exactamente
-- (ignorando mayusculas y espacios externos) con un unico administrador por
-- correo, codigo de empleado o "nombres apellidos". COUNT(DISTINCT ...) evita
-- vincular automaticamente nombres duplicados o coincidencias ambiguas.
UPDATE auditoria AS au
JOIN (
    SELECT coincidencias.auditoria_id,
           MIN(coincidencias.usuario_id) AS usuario_id
    FROM (
        SELECT a.id AS auditoria_id, u.id AS usuario_id
        FROM auditoria AS a
        JOIN usuario AS u
          ON LOWER(TRIM(a.responsable)) = LOWER(TRIM(u.correo))
        JOIN administrador AS ad ON ad.usuario_id = u.id

        UNION ALL

        SELECT a.id AS auditoria_id, u.id AS usuario_id
        FROM auditoria AS a
        JOIN administrador AS ad
          ON LOWER(TRIM(a.responsable)) = LOWER(TRIM(ad.codigo_empleado))
        JOIN usuario AS u ON u.id = ad.usuario_id

        UNION ALL

        SELECT a.id AS auditoria_id, u.id AS usuario_id
        FROM auditoria AS a
        JOIN usuario AS u
          ON LOWER(TRIM(a.responsable)) =
             LOWER(TRIM(CONCAT(u.nombres, ' ', u.apellidos)))
        JOIN administrador AS ad ON ad.usuario_id = u.id
    ) AS coincidencias
    GROUP BY coincidencias.auditoria_id
    HAVING COUNT(DISTINCT coincidencias.usuario_id) = 1
) AS unicas ON unicas.auditoria_id = au.id
SET au.responsable_id = unicas.usuario_id
WHERE au.responsable_id IS NULL;

-- El reporte es persistente para que los casos manuales puedan revisarse y
-- resolverse sin perder el texto original.
CREATE TABLE IF NOT EXISTS auditoria_responsable_migracion_pendiente (
    auditoria_id       INT PRIMARY KEY,
    responsable_texto  VARCHAR(150) NOT NULL,
    coincidencias      INT NOT NULL,
    motivo             VARCHAR(80) NOT NULL,
    revisado_en        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_auditoria_migracion_pendiente
        FOREIGN KEY (auditoria_id) REFERENCES auditoria(id) ON DELETE CASCADE
);

DELETE FROM auditoria_responsable_migracion_pendiente;

INSERT INTO auditoria_responsable_migracion_pendiente
    (auditoria_id, responsable_texto, coincidencias, motivo)
SELECT a.id,
       a.responsable,
       COUNT(DISTINCT candidatos.usuario_id),
       CASE
           WHEN COUNT(DISTINCT candidatos.usuario_id) = 0
               THEN 'SIN_COINCIDENCIA_ADMINISTRADOR'
           ELSE 'COINCIDENCIA_AMBIGUA'
       END
FROM auditoria AS a
LEFT JOIN (
    SELECT a1.id AS auditoria_id, u1.id AS usuario_id
    FROM auditoria AS a1
    JOIN usuario AS u1
      ON LOWER(TRIM(a1.responsable)) = LOWER(TRIM(u1.correo))
    JOIN administrador AS ad1 ON ad1.usuario_id = u1.id

    UNION

    SELECT a2.id AS auditoria_id, u2.id AS usuario_id
    FROM auditoria AS a2
    JOIN administrador AS ad2
      ON LOWER(TRIM(a2.responsable)) = LOWER(TRIM(ad2.codigo_empleado))
    JOIN usuario AS u2 ON u2.id = ad2.usuario_id

    UNION

    SELECT a3.id AS auditoria_id, u3.id AS usuario_id
    FROM auditoria AS a3
    JOIN usuario AS u3
      ON LOWER(TRIM(a3.responsable)) =
         LOWER(TRIM(CONCAT(u3.nombres, ' ', u3.apellidos)))
    JOIN administrador AS ad3 ON ad3.usuario_id = u3.id
) AS candidatos ON candidatos.auditoria_id = a.id
WHERE a.responsable_id IS NULL
GROUP BY a.id, a.responsable;

ALTER TABLE auditoria
    ADD CONSTRAINT fk_auditoria_responsable
        FOREIGN KEY (responsable_id)
        REFERENCES administrador(usuario_id)
        ON DELETE RESTRICT;

-- Resultados que deben adjuntarse a la revision de la migracion.
SELECT COUNT(*) AS auditorias_totales,
       SUM(responsable_id IS NOT NULL) AS vinculadas,
       SUM(responsable_id IS NULL) AS pendientes
FROM auditoria;

SELECT auditoria_id, responsable_texto, coincidencias, motivo, revisado_en
FROM auditoria_responsable_migracion_pendiente
ORDER BY auditoria_id;

-- Finalizacion manual (NO ejecutar mientras existan pendientes):
-- 1. Resolver cada fila con UPDATE auditoria SET responsable_id = ? WHERE id = ?;
-- 2. Repetir los SELECT de control y verificar la aplicacion.
-- 3. En una migracion posterior: responsable_id NOT NULL y DROP responsable.
