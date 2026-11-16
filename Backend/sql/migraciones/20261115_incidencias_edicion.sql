-- LAB12 / RF-M6-04
-- Ejecutar solamente despues de seleccionar una base de datos de pruebas.
-- Conserva id, fecha y funcion historicos; habilita editar descripcion,
-- responsable y estado sobre el mismo registro.

DELIMITER $$

DROP PROCEDURE IF EXISTS modificar_auditoria_atencion$$
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

    UPDATE auditoria
    SET descripcion = TRIM(p_descripcion),
        responsable_id = p_responsable_id,
        estado = p_estado
    WHERE id = p_id;
END$$

DELIMITER ;
