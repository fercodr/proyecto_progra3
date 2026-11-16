-- Pone la estructura de la base compartida al dia con el backend integrado en
-- el PR #27 (commit 76d94e5): columnas y tabla que leen cartelera, eventos,
-- sedes, salas y bloqueos.
--
-- Compatible con MySQL 8 y MariaDB. Se puede ejecutar mas de una vez: cada
-- cambio se aplica solo si falta. No borra ni modifica filas existentes.
--
-- ORDEN DE EJECUCION
--   1. Este archivo.
--   2. Backend/sql/procedimientos.sql completo (recrea vistas y los 78
--      procedimientos con el contrato que espera el backend actual).

USE entrefunciones;

-- sede.distrito
SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'sede'
        AND column_name = 'distrito') = 0,
    'ALTER TABLE sede ADD COLUMN distrito VARCHAR(100) NOT NULL DEFAULT ''Sin especificar''',
    'SELECT 1');
PREPARE st FROM @sql; EXECUTE st; DEALLOCATE PREPARE st;

-- sala.accesible
SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'sala'
        AND column_name = 'accesible') = 0,
    'ALTER TABLE sala ADD COLUMN accesible BOOLEAN NOT NULL DEFAULT FALSE',
    'SELECT 1');
PREPARE st FROM @sql; EXECUTE st; DEALLOCATE PREPARE st;

-- evento.tematica
SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'evento'
        AND column_name = 'tematica') = 0,
    'ALTER TABLE evento ADD COLUMN tematica VARCHAR(100) NULL',
    'SELECT 1');
PREPARE st FROM @sql; EXECUTE st; DEALLOCATE PREPARE st;

-- evento.publico_objetivo
SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'evento'
        AND column_name = 'publico_objetivo') = 0,
    'ALTER TABLE evento ADD COLUMN publico_objetivo VARCHAR(100) NULL',
    'SELECT 1');
PREPARE st FROM @sql; EXECUTE st; DEALLOCATE PREPARE st;

-- bloqueo_sala
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

-- Comprobacion: deben salir 5 filas.
SELECT table_name AS tabla, column_name AS columna
  FROM information_schema.columns
 WHERE table_schema = DATABASE()
   AND ((table_name = 'sede'   AND column_name = 'distrito')
     OR (table_name = 'sala'   AND column_name = 'accesible')
     OR (table_name = 'evento' AND column_name IN ('tematica', 'publico_objetivo')))
UNION ALL
SELECT table_name, '(tabla)'
  FROM information_schema.tables
 WHERE table_schema = DATABASE() AND table_name = 'bloqueo_sala';
