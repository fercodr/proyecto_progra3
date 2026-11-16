-- Instalación NUEVA. No elimina bases ni tablas existentes. MySQL 8 / MariaDB 10.11.
CREATE DATABASE IF NOT EXISTS entrefunciones CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE entrefunciones;

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

CREATE TABLE sede (
    id                 INT AUTO_INCREMENT PRIMARY KEY,
    razon_social       VARCHAR(150) NOT NULL,
    direccion          VARCHAR(200) NOT NULL,
    ciudad             VARCHAR(100) NOT NULL,
    distrito           VARCHAR(100) NOT NULL DEFAULT 'Sin especificar',
    telefono_contacto  VARCHAR(20),
    activo             BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE sala (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    sede_id    INT          NOT NULL,
    nombre     VARCHAR(100) NOT NULL,
    aforo      INT          NOT NULL,
    tipo_sala  VARCHAR(50)  NOT NULL,
    accesible  BOOLEAN      NOT NULL DEFAULT FALSE,
    activo     BOOLEAN      NOT NULL DEFAULT TRUE,
    UNIQUE KEY uk_sala_sede_nombre (sede_id, nombre),
    CONSTRAINT fk_sala_sede FOREIGN KEY (sede_id) REFERENCES sede (id),
    CONSTRAINT chk_sala_aforo CHECK (aforo > 0)
);

CREATE TABLE bloqueo_sala (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sala_id INT NOT NULL,
    inicio DATETIME NOT NULL,
    fin DATETIME NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    INDEX ix_bloqueo_sala_horario (sala_id, activo, inicio, fin),
    CONSTRAINT fk_bloqueo_sala_sala FOREIGN KEY (sala_id) REFERENCES sala(id),
    CONSTRAINT chk_bloqueo_sala_horario CHECK (fin > inicio)
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

CREATE TABLE evento (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    precio  DECIMAL(8,2) NOT NULL,
    activo  BOOLEAN      NOT NULL DEFAULT TRUE,
    imagen_url VARCHAR(255) NULL,  -- ruta relativa, p. ej. 'img/eventos/interestelar.jpg'
    tematica VARCHAR(100) NULL,
    publico_objetivo VARCHAR(100) NULL,
    CONSTRAINT chk_evento_precio CHECK (precio >= 0)
);

CREATE TABLE pelicula (
    evento_id          INT PRIMARY KEY,
    nombre_pelicula    VARCHAR(150) NOT NULL,
    duracion_minutos   INT          NOT NULL,
    genero             ENUM('ACCION','COMEDIA','DRAMA','TERROR','ANIMACION','DOCUMENTAL') NOT NULL,
    clasificacion_edad ENUM('APTA_TODO_PUBLICO','PG13','MAYORES_18') NOT NULL,
    director           VARCHAR(100),
    CONSTRAINT fk_pelicula_evento FOREIGN KEY (evento_id)
        REFERENCES evento (id) ON DELETE CASCADE,
    CONSTRAINT chk_pelicula_duracion CHECK (duracion_minutos > 0)
);

CREATE TABLE obra_teatro (
    evento_id        INT PRIMARY KEY,
    titulo           VARCHAR(150) NOT NULL,
    nombre_director  VARCHAR(100),
    elenco           VARCHAR(300),
    CONSTRAINT fk_obra_teatro_evento FOREIGN KEY (evento_id)
        REFERENCES evento (id) ON DELETE CASCADE
);

CREATE TABLE actividad_cultural (
    evento_id       INT PRIMARY KEY,
    nombre          VARCHAR(150) NOT NULL,
    expositor       VARCHAR(100),
    tipo_actividad  ENUM('CHARLA','TALLER','EXPOSICION','CONVERSATORIO') NOT NULL,
    CONSTRAINT fk_actividad_cultural_evento FOREIGN KEY (evento_id)
        REFERENCES evento (id) ON DELETE CASCADE
);

CREATE TABLE funcion (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    evento_id     INT           NOT NULL,
    sala_id       INT           NOT NULL,
    fecha         DATE          NOT NULL,
    hora_inicio   TIME          NOT NULL,
    hora_fin      TIME          NOT NULL,
    idioma        VARCHAR(30)   NOT NULL,
    estado        ENUM('PROGRAMADA','CONFIRMADA','CANCELADA') NOT NULL DEFAULT 'PROGRAMADA',
    CONSTRAINT fk_funcion_evento FOREIGN KEY (evento_id) REFERENCES evento (id),
    CONSTRAINT fk_funcion_sala FOREIGN KEY (sala_id) REFERENCES sala (id),
    CONSTRAINT chk_funcion_horario CHECK (hora_fin > hora_inicio)
);

CREATE TABLE reserva (
    id                 INT AUTO_INCREMENT PRIMARY KEY,
    funcion_id         INT       NOT NULL,
    cliente_id         INT       NOT NULL,
    cantidad           INT       NULL,
    precio_unitario    DECIMAL(8,2) NOT NULL,
    fecha_creacion     DATETIME  NOT NULL,
    fecha_expiracion   DATETIME  NOT NULL,
    estado             ENUM('ACTIVA','CONFIRMADA','EXPIRADA','CANCELADA') NOT NULL DEFAULT 'ACTIVA',
    CONSTRAINT fk_reserva_funcion FOREIGN KEY (funcion_id) REFERENCES funcion (id),
    CONSTRAINT fk_reserva_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (usuario_id),
    CONSTRAINT chk_reserva_expiracion CHECK (fecha_expiracion > fecha_creacion),
    CONSTRAINT chk_reserva_cantidad CHECK (cantidad IS NULL OR cantidad > 0)
);

CREATE TABLE reserva_butaca (
    reserva_id  INT NOT NULL,
    butaca_id   INT NOT NULL,
    PRIMARY KEY (reserva_id, butaca_id),
    UNIQUE KEY uk_reserva_butaca_par (reserva_id, butaca_id),
    CONSTRAINT fk_reserva_butaca_reserva FOREIGN KEY (reserva_id) REFERENCES reserva (id) ON DELETE CASCADE,
    CONSTRAINT fk_reserva_butaca_butaca FOREIGN KEY (butaca_id) REFERENCES butaca (id)
);

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

CREATE TABLE entrada (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    reserva_id     INT           NOT NULL,
    butaca_id      INT           NULL,
    fecha_emision  DATETIME      NOT NULL,
    codigo_qr      VARCHAR(100)  NOT NULL,
    precio_final   DECIMAL(8,2)  NOT NULL,
    estado         ENUM('VIGENTE','UTILIZADA','CANCELADA') NOT NULL DEFAULT 'VIGENTE',
    UNIQUE KEY uk_entrada_reserva_butaca (reserva_id, butaca_id),
    UNIQUE KEY uk_entrada_qr (codigo_qr),
    CONSTRAINT fk_entrada_reserva FOREIGN KEY (reserva_id) REFERENCES reserva (id),
    CONSTRAINT fk_entrada_reserva_butaca FOREIGN KEY (reserva_id, butaca_id)
        REFERENCES reserva_butaca (reserva_id, butaca_id),
    CONSTRAINT chk_entrada_precio CHECK (precio_final >= 0)
);

CREATE TABLE auditoria (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    funcion_id      INT           NULL,
    fecha           DATETIME      NOT NULL,
    responsable_id  INT           NULL,
    -- Respaldo temporal para filas legadas que todavia no puedan vincularse
    -- sin ambiguedad. Las altas nuevas no escriben esta columna.
    responsable     VARCHAR(150)  NULL,
    descripcion     VARCHAR(500)  NOT NULL,
    estado          ENUM('PENDIENTE','EN_PROCESO','RESUELTA') NOT NULL DEFAULT 'PENDIENTE',
    CONSTRAINT fk_auditoria_funcion FOREIGN KEY (funcion_id)
        REFERENCES funcion (id),
    CONSTRAINT fk_auditoria_responsable FOREIGN KEY (responsable_id)
        REFERENCES administrador (usuario_id) ON DELETE RESTRICT,
    CONSTRAINT chk_auditoria_responsable
        CHECK (responsable_id IS NOT NULL OR responsable IS NOT NULL)
);

CREATE INDEX ix_funcion_sala_fecha ON funcion(sala_id,fecha);
CREATE INDEX ix_reserva_funcion_estado ON reserva(funcion_id,estado,fecha_expiracion);
CREATE INDEX ix_auditoria_responsable ON auditoria(responsable_id);
