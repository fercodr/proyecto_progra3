USE entrefunciones;

DROP PROCEDURE IF EXISTS insertar_obra_teatro;
DROP PROCEDURE IF EXISTS modificar_obra_teatro;
DROP PROCEDURE IF EXISTS eliminar_obra_teatro;
DROP PROCEDURE IF EXISTS buscar_obra_teatro_por_id;
DROP PROCEDURE IF EXISTS listar_obras_teatro;

DELIMITER //

CREATE PROCEDURE insertar_obra_teatro(
    IN p_nombre VARCHAR(150),
    IN p_genero VARCHAR(50),
    IN p_clasificacion_edad VARCHAR(50),
    IN p_director VARCHAR(100),
    IN p_elenco VARCHAR(300),
    IN p_duracion_minutos INT,
    IN p_activo BOOLEAN,
    OUT p_id INT
)
BEGIN
    INSERT INTO contenido_catalogo (activo) VALUES (p_activo);
    SET p_id = LAST_INSERT_ID();
    
    INSERT INTO obra_teatro (id, nombre, genero, clasificacion_edad, director, elenco, duracion_minutos)
    VALUES (p_id, p_nombre, p_genero, p_clasificacion_edad, p_director, p_elenco, p_duracion_minutos);
END //

CREATE PROCEDURE modificar_obra_teatro(
    IN p_nombre VARCHAR(150),
    IN p_genero VARCHAR(50),
    IN p_clasificacion_edad VARCHAR(50),
    IN p_director VARCHAR(100),
    IN p_elenco VARCHAR(300),
    IN p_duracion_minutos INT,
    IN p_activo BOOLEAN,
    IN p_id INT
)
BEGIN
    UPDATE contenido_catalogo SET activo = p_activo WHERE id = p_id;
    UPDATE obra_teatro
    SET nombre = p_nombre,
        genero = p_genero,
        clasificacion_edad = p_clasificacion_edad,
        director = p_director,
        elenco = p_elenco,
        duracion_minutos = p_duracion_minutos
    WHERE id = p_id;
END //

CREATE PROCEDURE eliminar_obra_teatro(IN p_id INT)
BEGIN
    UPDATE contenido_catalogo SET activo = FALSE WHERE id = p_id;
END //

CREATE PROCEDURE buscar_obra_teatro_por_id(IN p_id INT)
BEGIN
    SELECT o.id, o.nombre, o.genero, o.clasificacion_edad, o.director, o.elenco, o.duracion_minutos, c.activo
    FROM obra_teatro o
    INNER JOIN contenido_catalogo c ON o.id = c.id
    WHERE o.id = p_id;
END //

CREATE PROCEDURE listar_obras_teatro()
BEGIN
    SELECT o.id, o.nombre, o.genero, o.clasificacion_edad, o.director, o.elenco, o.duracion_minutos, c.activo
    FROM obra_teatro o
    INNER JOIN contenido_catalogo c ON o.id = c.id;
END //

DELIMITER ;