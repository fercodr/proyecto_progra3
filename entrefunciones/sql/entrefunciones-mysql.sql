-- =====================================================================
-- EntreFunciones — Script de base de datos (MySQL 8.0 / MariaDB 10.5+)
-- Equipo: Los que no compilan — Programación 3
--
-- Deriva directamente del modelo de dominio en Java (paquetes comun,
-- usuarios, espacios, catalogo, funciones, ventas, auditoria). Decisiones
-- de mapeo objeto-relacional documentadas junto a cada tabla:
--
--   - Herencia Usuario/Administrador/Cliente y
--     Funcion/Pelicula/ObraTeatro/ActividadCultural: tabla por subtipo (la
--     tabla base guarda los atributos comunes; cada subtipo comparte su
--     clave primaria con la base mediante una FK 1 a 1).
--   - Enumeraciones Java (EstadoFuncion, EstadoReserva, EstadoPago,
--     EstadoEntrada, EstadoAuditoria, CategoriaGenero, ClasificacionEdad,
--     TipoActividad): tipo ENUM nativo de MySQL, con los mismos valores
--     definidos en el código.
--   - Reserva "1" --> "*" Butaca: tabla puente reserva_butaca.
--   - Entrada "*" --> "1" Reserva y "*" --> "1" Butaca: en el modelo de
--     dominio son dos asociaciones independientes, pero una Entrada
--     siempre corresponde a una butaca que efectivamente fue parte de la
--     Reserva que la origina; por eso entrada referencia el PAR
--     (reserva_id, butaca_id) con una FK compuesta hacia reserva_butaca,
--     en lugar de dos FK sueltas. Esto impide emitir una entrada para una
--     butaca que nunca se reservó.
--   - NOTA: al diseñar este script se detectaron relaciones del diagrama
--     de clases que el código Java todavía no implementaba (Funcion sin
--     atributo `sala`, Auditoria sin forma de indicar a qué Funcion
--     pertenece una incidencia, entre otras). Ya se corrigieron en el
--     módulo de dominio (entrefunciones-modelo); columnas como
--     funcion.sala_id y auditoria.funcion_id de este script tienen su
--     equivalente en Java.
-- =====================================================================

DROP DATABASE IF EXISTS entrefunciones;
CREATE DATABASE entrefunciones CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE entrefunciones;

-- ---------------------------------------------------------------------
-- Paquete usuarios: Usuario (abstracta) + Administrador / Cliente
-- ---------------------------------------------------------------------

CREATE TABLE usuario (
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

-- Extiende usuario (tabla por subtipo): un Administrador SIEMPRE es un Usuario.
CREATE TABLE administrador (
    usuario_id      INT PRIMARY KEY,
    codigo_empleado VARCHAR(20) NOT NULL,
    UNIQUE KEY uk_administrador_codigo (codigo_empleado),
    CONSTRAINT fk_administrador_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuario (id) ON DELETE CASCADE
);

CREATE TABLE cliente (
    usuario_id     INT PRIMARY KEY,
    fecha_registro DATE        NOT NULL,
    telefono       VARCHAR(20),
    CONSTRAINT fk_cliente_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuario (id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- Paquete espacios: Sede, Sala, Butaca, Recurso, Bloqueo
-- ---------------------------------------------------------------------

CREATE TABLE sede (
    id                 INT AUTO_INCREMENT PRIMARY KEY,
    razon_social       VARCHAR(150) NOT NULL,
    direccion          VARCHAR(200) NOT NULL,
    ciudad             VARCHAR(100) NOT NULL,
    telefono_contacto  VARCHAR(20),
    activo             BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE sala (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    sede_id    INT          NOT NULL,
    nombre     VARCHAR(100) NOT NULL,
    aforo      INT          NOT NULL,
    tipo_sala  VARCHAR(50)  NOT NULL,
    activo     BOOLEAN      NOT NULL DEFAULT TRUE,
    UNIQUE KEY uk_sala_sede_nombre (sede_id, nombre),
    CONSTRAINT fk_sala_sede FOREIGN KEY (sede_id) REFERENCES sede (id),
    CONSTRAINT chk_sala_aforo CHECK (aforo > 0)
);

CREATE TABLE butaca (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    sala_id      INT         NOT NULL,
    fila         VARCHAR(5)  NOT NULL,
    columna      INT         NOT NULL,
    tipo_butaca  VARCHAR(30) NOT NULL,
    activo       BOOLEAN     NOT NULL DEFAULT TRUE,
    UNIQUE KEY uk_butaca_sala_pos (sala_id, fila, columna),
    CONSTRAINT fk_butaca_sala FOREIGN KEY (sala_id) REFERENCES sala (id)
);

CREATE TABLE recurso (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    nombre  VARCHAR(100) NOT NULL,
    tipo    VARCHAR(50)  NOT NULL,
    activo  BOOLEAN      NOT NULL DEFAULT TRUE
);

-- Bloquea una Sala O un Recurso (nunca ambos, nunca ninguno): RF-M2-02.
CREATE TABLE bloqueo (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    motivo        VARCHAR(200)  NOT NULL,
    fecha_inicio  DATETIME      NOT NULL,
    fecha_fin     DATETIME      NOT NULL,
    sala_id       INT           NULL,
    recurso_id    INT           NULL,
    CONSTRAINT fk_bloqueo_sala FOREIGN KEY (sala_id) REFERENCES sala (id),
    CONSTRAINT fk_bloqueo_recurso FOREIGN KEY (recurso_id) REFERENCES recurso (id),
    CONSTRAINT chk_bloqueo_fechas CHECK (fecha_fin > fecha_inicio),
    CONSTRAINT chk_bloqueo_objetivo_unico CHECK (
        (sala_id IS NOT NULL AND recurso_id IS NULL) OR
        (sala_id IS NULL AND recurso_id IS NOT NULL)
    )
);

-- ---------------------------------------------------------------------
-- Paquete funciones: Funcion (tabla BASE de la herencia Funcion ->
-- Pelicula/ObraTeatro/ActividadCultural; ver paquete catalogo mas abajo).
-- Ya no tiene contenido_id: el contenido programado ya no es una
-- referencia aparte, es la propia fila de pelicula/obra_teatro/
-- actividad_cultural que comparte este mismo id.
-- ---------------------------------------------------------------------

CREATE TABLE funcion (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    sala_id       INT           NOT NULL,
    fecha         DATE          NOT NULL,
    hora_inicio   TIME          NOT NULL,
    hora_fin      TIME          NOT NULL,
    precio        DECIMAL(8,2)  NOT NULL,
    idioma        VARCHAR(30)   NOT NULL,
    estado        ENUM('PROGRAMADA','CONFIRMADA','CANCELADA') NOT NULL DEFAULT 'PROGRAMADA',
    CONSTRAINT fk_funcion_sala FOREIGN KEY (sala_id) REFERENCES sala (id),
    CONSTRAINT chk_funcion_horario CHECK (hora_fin > hora_inicio),
    CONSTRAINT chk_funcion_precio CHECK (precio >= 0)
);

CREATE INDEX ix_funcion_fecha ON funcion (fecha);
CREATE INDEX ix_funcion_sala_fecha ON funcion (sala_id, fecha);

-- ---------------------------------------------------------------------
-- Paquete catalogo: Pelicula / ObraTeatro / ActividadCultural heredan de
-- Funcion (tabla por subtipo: su PK es a la vez FK 1 a 1 hacia funcion).
-- Cada una tiene su propio `activo` (eliminado logico, IEliminable), que
-- es independiente del `estado` de la funcion (flujo de negocio de
-- programacion) heredado de la tabla base.
-- ---------------------------------------------------------------------

CREATE TABLE pelicula (
    funcion_id         INT PRIMARY KEY,
    nombre_pelicula    VARCHAR(150) NOT NULL,
    duracion_minutos   INT          NOT NULL,
    genero             ENUM('ACCION','COMEDIA','DRAMA','TERROR','ANIMACION','DOCUMENTAL') NOT NULL,
    clasificacion_edad ENUM('APTA_TODO_PUBLICO','PG13','MAYORES_18') NOT NULL,
    director           VARCHAR(100),
    activo             BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_pelicula_funcion FOREIGN KEY (funcion_id)
        REFERENCES funcion (id) ON DELETE CASCADE,
    CONSTRAINT chk_pelicula_duracion CHECK (duracion_minutos > 0)
);

CREATE TABLE obra_teatro (
    funcion_id       INT PRIMARY KEY,
    titulo           VARCHAR(150) NOT NULL,
    nombre_director  VARCHAR(100),
    elenco           VARCHAR(300),
    activo           BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_obra_teatro_funcion FOREIGN KEY (funcion_id)
        REFERENCES funcion (id) ON DELETE CASCADE
);

CREATE TABLE actividad_cultural (
    funcion_id      INT PRIMARY KEY,
    nombre          VARCHAR(150) NOT NULL,
    expositor       VARCHAR(100),
    tipo_actividad  ENUM('CHARLA','TALLER','EXPOSICION','CONVERSATORIO') NOT NULL,
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_actividad_cultural_funcion FOREIGN KEY (funcion_id)
        REFERENCES funcion (id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- Paquete ventas: Reserva, Pago, Entrada
-- ---------------------------------------------------------------------

CREATE TABLE reserva (
    id                 INT AUTO_INCREMENT PRIMARY KEY,
    funcion_id         INT       NOT NULL,
    cliente_id         INT       NOT NULL,
    fecha_creacion     DATETIME  NOT NULL,
    fecha_expiracion   DATETIME  NOT NULL,
    estado             ENUM('ACTIVA','CONFIRMADA','EXPIRADA','CANCELADA') NOT NULL DEFAULT 'ACTIVA',
    CONSTRAINT fk_reserva_funcion FOREIGN KEY (funcion_id) REFERENCES funcion (id),
    CONSTRAINT fk_reserva_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (usuario_id),
    CONSTRAINT chk_reserva_expiracion CHECK (fecha_expiracion > fecha_creacion)
);

CREATE INDEX ix_reserva_estado ON reserva (estado);

-- Tabla puente: una Reserva ocupa varias Butacas de la misma Funcion.
CREATE TABLE reserva_butaca (
    reserva_id  INT NOT NULL,
    butaca_id   INT NOT NULL,
    PRIMARY KEY (reserva_id, butaca_id),
    UNIQUE KEY uk_reserva_butaca_par (reserva_id, butaca_id),
    CONSTRAINT fk_reserva_butaca_reserva FOREIGN KEY (reserva_id) REFERENCES reserva (id) ON DELETE CASCADE,
    CONSTRAINT fk_reserva_butaca_butaca FOREIGN KEY (butaca_id) REFERENCES butaca (id)
);

DELIMITER $$
-- Regla de negocio central de la venta: una butaca no puede estar en dos
-- reservas ACTIVAS/CONFIRMADAS a la vez para la MISMA funcion (sigue
-- pudiendo reservarse libremente en otras funciones de la misma sala).
CREATE TRIGGER trg_reserva_butaca_sin_traslape
BEFORE INSERT ON reserva_butaca
FOR EACH ROW
BEGIN
    DECLARE v_funcion_id INT;
    DECLARE v_conflictos INT;

    SELECT funcion_id INTO v_funcion_id FROM reserva WHERE id = NEW.reserva_id;

    SELECT COUNT(*) INTO v_conflictos
    FROM reserva_butaca rb
    INNER JOIN reserva r ON r.id = rb.reserva_id
    WHERE rb.butaca_id = NEW.butaca_id
      AND r.funcion_id = v_funcion_id
      AND r.estado IN ('ACTIVA', 'CONFIRMADA')
      AND rb.reserva_id <> NEW.reserva_id;

    IF v_conflictos > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'La butaca ya esta reservada/confirmada para esta funcion';
    END IF;
END$$
DELIMITER ;

-- Reserva "1" --> "1" Pago (relación uno a uno: reserva_id es UNIQUE).
CREATE TABLE pago (
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

-- Entrada referencia el PAR (reserva_id, butaca_id) que sí fue reservado
-- (FK compuesta hacia reserva_butaca), no dos FK sueltas.
CREATE TABLE entrada (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    reserva_id     INT           NOT NULL,
    butaca_id      INT           NOT NULL,
    fecha_emision  DATETIME      NOT NULL,
    codigo_qr      VARCHAR(100)  NOT NULL,
    precio_final   DECIMAL(8,2)  NOT NULL,
    estado         ENUM('VIGENTE','UTILIZADA','CANCELADA') NOT NULL DEFAULT 'VIGENTE',
    UNIQUE KEY uk_entrada_reserva_butaca (reserva_id, butaca_id),
    UNIQUE KEY uk_entrada_qr (codigo_qr),
    CONSTRAINT fk_entrada_reserva_butaca FOREIGN KEY (reserva_id, butaca_id)
        REFERENCES reserva_butaca (reserva_id, butaca_id),
    CONSTRAINT chk_entrada_precio CHECK (precio_final >= 0)
);

-- ---------------------------------------------------------------------
-- Paquete auditoria
-- ---------------------------------------------------------------------

-- funcion_id es opcional porque Auditoria también sustenta RNF-SEG-01
-- (trazabilidad de operaciones críticas que no siempre giran en torno a
-- una Funcion puntual, p.ej. inicios de sesión o cambios administrativos).
CREATE TABLE auditoria (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    funcion_id   INT           NULL,
    fecha        DATETIME      NOT NULL,
    responsable  VARCHAR(150)  NOT NULL,
    descripcion  VARCHAR(500)  NOT NULL,
    estado       ENUM('PENDIENTE','EN_PROCESO','RESUELTA') NOT NULL DEFAULT 'PENDIENTE',
    CONSTRAINT fk_auditoria_funcion FOREIGN KEY (funcion_id) REFERENCES funcion (id)
);

-- =====================================================================
-- Datos de prueba
--
-- Cubren cada tabla y cada valor de cada enumeración. Incluyen
-- deliberadamente la MISMA película (Interestelar) programada en DOS
-- funciones distintas (funcion.id = 1 y 3): como Pelicula hereda de
-- Funcion, eso significa DOS filas en `pelicula` (funcion_id = 1 y
-- funcion_id = 3) que repiten titulo, duracion, genero, clasificacion y
-- director.
-- =====================================================================

-- Usuarios: 1 administrador + 3 clientes.
INSERT INTO usuario (id, nombres, apellidos, correo, contrasena, dni, activo) VALUES
    (1, 'Marco',    'Reyes',    'marco.reyes@entrefunciones.pe',   'hash_admin1',   '70011122', TRUE),
    (2, 'Valeria',  'Suarez',   'valeria.suarez@gmail.com',        'hash_cliente1', '71122334', TRUE),
    (3, 'Diego',    'Palacios', 'diego.palacios@gmail.com',        'hash_cliente2', '72233445', TRUE),
    (4, 'Camila',   'Torres',   'camila.torres@gmail.com',         'hash_cliente3', '73344556', FALSE);

INSERT INTO administrador (usuario_id, codigo_empleado) VALUES
    (1, 'EMP-0001');

INSERT INTO cliente (usuario_id, fecha_registro, telefono) VALUES
    (2, '2026-01-15', '987654321'),
    (3, '2026-02-03', '987654322'),
    (4, '2025-11-20', '987654323');  -- cuenta desactivada (eliminado logico)

-- Sedes y salas.
INSERT INTO sede (id, razon_social, direccion, ciudad, telefono_contacto, activo) VALUES
    (1, 'EntreFunciones Miraflores S.A.C.', 'Av. Larco 1234', 'Lima', '014001122', TRUE),
    (2, 'EntreFunciones San Isidro S.A.C.', 'Av. Javier Prado 4321', 'Lima', '014003344', TRUE);

INSERT INTO sala (id, sede_id, nombre, aforo, tipo_sala, activo) VALUES
    (1, 1, 'Sala 1', 80, 'Cine 2D', TRUE),
    (2, 1, 'Sala 2', 40, 'Teatro',  TRUE),
    (3, 2, 'Sala 1', 60, 'Cine 3D', TRUE);

INSERT INTO butaca (id, sala_id, fila, columna, tipo_butaca, activo) VALUES
    (1, 1, 'A', 1, 'General', TRUE),
    (2, 1, 'A', 2, 'General', TRUE),
    (3, 1, 'A', 3, 'VIP',     TRUE),
    (4, 2, 'A', 1, 'General', TRUE),
    (5, 2, 'A', 2, 'General', FALSE); -- dada de baja (p.ej. butaca dañada)

INSERT INTO recurso (id, nombre, tipo, activo) VALUES
    (1, 'Proyector 4K Sala 1', 'Proyeccion', TRUE),
    (2, 'Equipo de sonido portatil', 'Audio', TRUE);

INSERT INTO bloqueo (id, motivo, fecha_inicio, fecha_fin, sala_id, recurso_id) VALUES
    (1, 'Mantenimiento preventivo de butacas', '2026-09-20 08:00:00', '2026-09-20 12:00:00', 2, NULL),
    (2, 'Calibracion de proyector', '2026-09-18 09:00:00', '2026-09-18 11:00:00', NULL, 1);

-- Funciones: 5 en total. La 1 y la 3 son la MISMA pelicula (Interestelar)
-- en dos horarios distintos; cada una tendra su propia fila en `pelicula`.
INSERT INTO funcion (id, sala_id, fecha, hora_inicio, hora_fin, precio, idioma, estado) VALUES
    (1, 1, '2026-09-20', '15:00:00', '17:49:00', 25.00, 'Español',     'CONFIRMADA'),
    (2, 1, '2026-09-20', '20:00:00', '21:35:00', 22.00, 'Subtitulada', 'PROGRAMADA'),
    (3, 1, '2026-09-20', '18:00:00', '20:49:00', 28.00, 'Subtitulada', 'CONFIRMADA'),
    (4, 2, '2026-09-21', '19:00:00', '21:00:00', 35.00, 'Español',     'PROGRAMADA'),
    (5, 3, '2026-09-19', '17:00:00', '18:30:00', 10.00, 'Español',     'CANCELADA');

-- Catalogo: Interestelar se repite (funcion_id 1 y 3) porque Pelicula
-- hereda de Funcion; cada horario es su propia fila de pelicula.
INSERT INTO pelicula (funcion_id, nombre_pelicula, duracion_minutos, genero, clasificacion_edad, director, activo) VALUES
    (1, 'Interestelar', 169, 'DRAMA', 'PG13', 'Christopher Nolan', TRUE),
    (2, 'La Casa del Terror', 95, 'TERROR', 'MAYORES_18', 'Ana Ramirez', TRUE),
    (3, 'Interestelar', 169, 'DRAMA', 'PG13', 'Christopher Nolan', TRUE);

INSERT INTO obra_teatro (funcion_id, titulo, nombre_director, elenco, activo) VALUES
    (4, 'La Muralla', 'Sofia Herrera', 'Juan Perez, Maria Lopez', TRUE);

INSERT INTO actividad_cultural (funcion_id, nombre, expositor, tipo_actividad, activo) VALUES
    (5, 'Charla de Cine Peruano Contemporaneo', 'Rodrigo Vega', 'CHARLA', TRUE);

-- Reservas: una confirmada (flujo completo), una pendiente de pago, una expirada.
INSERT INTO reserva (id, funcion_id, cliente_id, fecha_creacion, fecha_expiracion, estado) VALUES
    (1, 1, 2, '2026-09-14 10:00:00', '2026-09-14 10:15:00', 'CONFIRMADA'),
    (2, 3, 3, '2026-09-14 11:00:00', '2026-09-14 11:15:00', 'ACTIVA'),
    (3, 2, 2, '2026-09-10 09:00:00', '2026-09-10 09:15:00', 'EXPIRADA');

INSERT INTO reserva_butaca (reserva_id, butaca_id) VALUES
    (1, 1), (1, 2),   -- Valeria reservo A1 y A2 para la funcion 1
    (2, 3),           -- Diego reservo A3 (VIP) para la funcion 3
    (3, 1);           -- reserva vencida de Valeria para la funcion 2 (butaca A1, otra funcion: sin conflicto)

-- Pagos: uno validado, uno pendiente, uno rechazado (para la reserva vencida).
INSERT INTO pago (id, reserva_id, codigo_pago, monto, fecha_pago, estado) VALUES
    (1, 1, 'PAY-0001', 50.00, '2026-09-14 10:05:00', 'VALIDADO'),
    (2, 2, 'PAY-0002', 28.00, NULL, 'PENDIENTE'),
    (3, 3, 'PAY-0003', 25.00, '2026-09-10 09:10:00', 'RECHAZADO');

-- Entradas: solo se emiten para la reserva ya confirmada/pagada (reserva 1).
INSERT INTO entrada (id, reserva_id, butaca_id, fecha_emision, codigo_qr, precio_final, estado) VALUES
    (1, 1, 1, '2026-09-14 10:05:30', 'QR-0000000001', 25.00, 'VIGENTE'),
    (2, 1, 2, '2026-09-14 10:05:30', 'QR-0000000002', 25.00, 'VIGENTE');

-- Auditoria: una incidencia ligada a una funcion (RF-M6-04) y un registro
-- de trazabilidad general sin funcion asociada (RNF-SEG-01).
INSERT INTO auditoria (id, funcion_id, fecha, responsable, descripcion, estado) VALUES
    (1, 1, '2026-09-14 10:06:00', 'Marco Reyes', 'Cliente reporto butaca A2 con reposabrazos suelto durante la funcion.', 'PENDIENTE'),
    (2, NULL, '2026-09-14 09:58:00', 'valeria.suarez@gmail.com', 'Inicio de sesion exitoso desde nueva ubicacion.', 'RESUELTA');
