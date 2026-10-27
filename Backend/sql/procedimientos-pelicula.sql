USE entrefunciones;

DROP PROCEDURE IF EXISTS insertar_pelicula;
DROP PROCEDURE IF EXISTS modificar_pelicula;
DROP PROCEDURE IF EXISTS eliminar_pelicula;
DROP PROCEDURE IF EXISTS buscar_pelicula_por_id;
DROP PROCEDURE IF EXISTS listar_peliculas;

DELIMITER //

CREATE PROCEDURE insertar_pelicula(
    IN p_nombre VARCHAR(150),
    IN p_genero VARCHAR(50),
    IN p_clasificacion_edad VARCHAR(50),
    IN p_director VARCHAR(100),
    IN p_duracion_minutos INT,
    IN p_activo BOOLEAN,
    OUT p_id INT
)
BEGIN
    INSERT INTO contenido_catalogo (activo) VALUES (p_activo);
    SET p_id = LAST_INSERT_ID();
    
    INSERT INTO pelicula (id, nombre_pelicula, genero, clasificacion_edad, director, duracion_minutos)
    VALUES (p_id, p_nombre, p_genero, p_clasificacion_edad, p_director, p_duracion_minutos);
END //

CREATE PROCEDURE modificar_pelicula(
    IN p_nombre VARCHAR(150),
    IN p_genero VARCHAR(50),
    IN p_clasificacion_edad VARCHAR(50),
    IN p_director VARCHAR(100),
    IN p_duracion_minutos INT,
    IN p_activo BOOLEAN,
    IN p_id INT
)
BEGIN
    UPDATE contenido_catalogo SET activo = p_activo WHERE id = p_id;
    UPDATE pelicula
    SET nombre_pelicula = p_nombre,
        genero = p_genero,
        clasificacion_edad = p_clasificacion_edad,
        director = p_director,
        duracion_minutos = p_duracion_minutos
    WHERE id = p_id;
END //

CREATE PROCEDURE eliminar_pelicula(IN p_id INT)
BEGIN
    UPDATE contenido_catalogo SET activo = FALSE WHERE id = p_id;
END //

CREATE PROCEDURE buscar_pelicula_por_id(IN p_id INT)
BEGIN
    SELECT p.id, p.nombre_pelicula, p.genero, p.clasificacion_edad, p.director, p.duracion_minutos, c.activo
    FROM pelicula p
    INNER JOIN contenido_catalogo c ON p.id = c.id
    WHERE p.id = p_id;
END //

CREATE PROCEDURE listar_peliculas()
BEGIN
    SELECT p.id, p.nombre_pelicula, p.genero, p.clasificacion_edad, p.director, p.duracion_minutos, c.activo
    FROM pelicula p
    INNER JOIN contenido_catalogo c ON p.id = c.id;
END //

DELIMITER ;