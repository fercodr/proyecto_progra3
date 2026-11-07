# Catálogo de procedimientos almacenados vigente

Fuente: `Backend/sql/procedimientos.sql`. Hay 68 definiciones únicas. Se verificó estáticamente la existencia y cantidad de argumentos de cada llamada literal de los DAO y la presencia de los procedimientos elegidos por sus métodos de nombres. Las pruebas de integración ejecutan los contratos usados en programación y ventas, además de consultas de usuarios, espacios y eventos. No todos los procedimientos de mantenimiento se ejecutaron individualmente.

ButacaDAOImpl y algunas operaciones de SedeDAOImpl/SalaDAOImpl utilizan SQL parametrizado directamente: no requieren los SP antiguos de butacas.

| Procedimiento | Parámetros IN OUT | DAO que lo referencia |
|---|---|---|
| `listar_sedes` | Sin parámetros | `SedeDAOImpl.java` |
| `buscar_sede_por_id` | IN p_id INT | `SedeDAOImpl.java` |
| `buscar_sede_por_razon_social` | IN p_razon_social VARCHAR(150) |  |
| `insertar_sede` | IN p_razon_social VARCHAR(150),     IN p_direccion VARCHAR(200),     IN p_ciudad VARCHAR(80),     IN p_telefono_contacto VARCHAR(20),     IN p_activo BOOLEAN,     OUT p_id INT | `SedeDAOImpl.java` |
| `modificar_sede` | IN p_id INT,     IN p_razon_social VARCHAR(150),     IN p_direccion VARCHAR(200),     IN p_ciudad VARCHAR(80),     IN p_telefono_contacto VARCHAR(20),     IN p_activo BOOLEAN | `SedeDAOImpl.java` |
| `eliminar_sede` | IN p_id INT | `SedeDAOImpl.java` |
| `listar_salas` | Sin parámetros | `SalaDAOImpl.java` |
| `buscar_sala_por_id` | IN p_id INT | `SalaDAOImpl.java` |
| `listar_salas_por_sede` | IN p_sede_id INT | `SalaDAOImpl.java` |
| `insertar_sala` | IN p_sede_id INT,     IN p_nombre VARCHAR(100),     IN p_aforo INT,     IN p_tipo_sala VARCHAR(50),     IN p_activo BOOLEAN,     OUT p_id INT | `SalaDAOImpl.java` |
| `modificar_sala` | IN p_id INT,     IN p_sede_id INT,     IN p_nombre VARCHAR(100),     IN p_aforo INT,     IN p_tipo_sala VARCHAR(50),     IN p_activo BOOLEAN | `SalaDAOImpl.java` |
| `eliminar_sala` | IN p_id INT | `SalaDAOImpl.java` |
| `listar_clientes` | Sin parámetros | `ClienteDAOImpl.java` |
| `buscar_cliente_por_id` | IN p_id INT | `ClienteDAOImpl.java` |
| `buscar_cliente_por_correo` | IN p_correo VARCHAR(150) | `ClienteDAOImpl.java` |
| `insertar_cliente` | IN p_nombres VARCHAR(100),     IN p_apellidos VARCHAR(100),     IN p_correo VARCHAR(150),     IN p_contrasena VARCHAR(255),     IN p_dni VARCHAR(15),     IN p_activo BOOLEAN,     IN p_fecha_registro DATE,     IN p_telefono VARCHAR(20),     OUT p_id INT | `ClienteDAOImpl.java` |
| `modificar_cliente` | IN p_id INT,     IN p_nombres VARCHAR(100),     IN p_apellidos VARCHAR(100),     IN p_correo VARCHAR(150),     IN p_contrasena VARCHAR(255),     IN p_dni VARCHAR(15),     IN p_activo BOOLEAN,     IN p_fecha_registro DATE,     IN p_telefono VARCHAR(20) | `ClienteDAOImpl.java` |
| `eliminar_cliente` | IN p_id INT | `ClienteDAOImpl.java` |
| `buscar_cliente_por_dni` | IN p_dni VARCHAR(15) | `ClienteDAOImpl.java` |
| `buscar_administrador_por_dni` | IN p_dni VARCHAR(15) | `AdministradorDAOImpl.java` |
| `listar_administradores` | Sin parámetros | `AdministradorDAOImpl.java` |
| `buscar_administrador_por_id` | IN p_id INT | `AdministradorDAOImpl.java` |
| `buscar_administrador_por_correo` | IN p_correo VARCHAR(150) | `AdministradorDAOImpl.java` |
| `buscar_administrador_por_codigo` | IN p_codigo_empleado VARCHAR(20) | `AdministradorDAOImpl.java` |
| `insertar_administrador` | IN p_nombres VARCHAR(100),IN p_apellidos VARCHAR(100),IN p_correo VARCHAR(150),IN p_contrasena VARCHAR(255),IN p_dni VARCHAR(15),IN p_activo BOOLEAN,IN p_codigo_empleado VARCHAR(20),OUT p_id INT | `AdministradorDAOImpl.java` |
| `modificar_administrador` | IN p_nombres VARCHAR(100),IN p_apellidos VARCHAR(100),IN p_correo VARCHAR(150),IN p_contrasena VARCHAR(255),IN p_dni VARCHAR(15),IN p_activo BOOLEAN,IN p_codigo_empleado VARCHAR(20),IN p_id INT | `AdministradorDAOImpl.java` |
| `eliminar_administrador` | IN p_id INT | `AdministradorDAOImpl.java` |
| `listar_eventos` | Sin parámetros | `EventoDAOImpl.java` |
| `buscar_evento_por_id` | IN p_id INT | `EventoDAOImpl.java` |
| `insertar_evento` | IN p_precio DECIMAL(8,2),IN p_activo BOOLEAN,OUT p_id INT | `EventoDAOImpl.java` |
| `modificar_evento` | IN p_id INT,IN p_precio DECIMAL(8,2),IN p_activo BOOLEAN | `EventoDAOImpl.java` |
| `eliminar_evento` | IN p_id INT | `EventoDAOImpl.java` |
| `insertar_evento_pelicula` | IN p_evento_id INT,IN p_nombre_pelicula VARCHAR(150),IN p_duracion_minutos INT,IN p_genero VARCHAR(30),IN p_clasificacion_edad VARCHAR(30),IN p_director VARCHAR(100) | `EventoDAOImpl.java` |
| `modificar_evento_pelicula` | IN p_evento_id INT,IN p_nombre_pelicula VARCHAR(150),IN p_duracion_minutos INT,IN p_genero VARCHAR(30),IN p_clasificacion_edad VARCHAR(30),IN p_director VARCHAR(100) | `EventoDAOImpl.java` |
| `insertar_evento_obra_teatro` | IN p_evento_id INT,IN p_titulo VARCHAR(150),IN p_nombre_director VARCHAR(100),IN p_elenco VARCHAR(300) | `EventoDAOImpl.java` |
| `modificar_evento_obra_teatro` | IN p_evento_id INT,IN p_titulo VARCHAR(150),IN p_nombre_director VARCHAR(100),IN p_elenco VARCHAR(300) | `EventoDAOImpl.java` |
| `insertar_evento_actividad_cultural` | IN p_evento_id INT,IN p_nombre VARCHAR(150),IN p_expositor VARCHAR(100),IN p_tipo_actividad VARCHAR(30) | `EventoDAOImpl.java` |
| `modificar_evento_actividad_cultural` | IN p_evento_id INT,IN p_nombre VARCHAR(150),IN p_expositor VARCHAR(100),IN p_tipo_actividad VARCHAR(30) | `EventoDAOImpl.java` |
| `listar_funciones` | Sin parámetros | `FuncionDAOImpl.java` |
| `buscar_funcion_por_id` | IN p_id INT | `FuncionDAOImpl.java` |
| `listar_funciones_por_evento` | IN p_evento_id INT | `FuncionDAOImpl.java` |
| `listar_funciones_por_sala` | IN p_sala_id INT | `FuncionDAOImpl.java` |
| `insertar_funcion` | IN p_evento_id INT,IN p_sala_id INT,IN p_fecha DATE,IN p_hora_inicio TIME,IN p_hora_fin TIME,IN p_idioma VARCHAR(30),IN p_estado VARCHAR(20),OUT p_id INT | `FuncionDAOImpl.java` |
| `modificar_funcion` | IN p_id INT,IN p_sala_id INT,IN p_fecha DATE,IN p_hora_inicio TIME,IN p_hora_fin TIME,IN p_idioma VARCHAR(30),IN p_estado VARCHAR(20) | `FuncionDAOImpl.java` |
| `cancelar_funcion` | IN p_id INT | `FuncionDAOImpl.java` |
| `cancelar_funciones_por_evento` | IN p_evento_id INT | `FuncionDAOImpl.java` |
| `listar_reservas` | Sin parámetros | `ReservaDAOImpl.java` |
| `buscar_reserva_por_id` | IN p_id INT | `ReservaDAOImpl.java` |
| `listar_reservas_por_cliente` | IN p_cliente_id INT | `ReservaDAOImpl.java` |
| `listar_reservas_por_funcion` | IN p_funcion_id INT | `ReservaDAOImpl.java` |
| `insertar_reserva` | IN p_funcion_id INT,IN p_cliente_id INT,IN p_fecha_creacion DATETIME,IN p_fecha_expiracion DATETIME,IN p_estado VARCHAR(20),IN p_cantidad INT,IN p_precio_unitario DECIMAL(8,2),OUT p_id INT | `ReservaDAOImpl.java` |
| `insertar_reserva_butaca` | IN p_reserva_id INT,IN p_butaca_id INT | `ReservaDAOImpl.java` |
| `modificar_reserva` | IN p_id INT,IN p_estado VARCHAR(20) | `ReservaDAOImpl.java` |
| `eliminar_reserva` | IN p_id INT | `ReservaDAOImpl.java` |
| `listar_pagos` | Sin parámetros | `PagoDAOImpl.java` |
| `buscar_pago_por_id` | IN p_id INT | `PagoDAOImpl.java` |
| `buscar_pago_por_reserva` | IN p_reserva_id INT | `PagoDAOImpl.java` |
| `buscar_pago_por_codigo` | IN p_codigo_pago VARCHAR(50) | `PagoDAOImpl.java` |
| `insertar_pago` | IN p_reserva_id INT,     IN p_codigo_pago VARCHAR(50),     IN p_monto DECIMAL(8,2),     IN p_fecha_pago DATETIME,     IN p_estado VARCHAR(20),     OUT p_id INT | `PagoDAOImpl.java` |
| `modificar_pago` | IN p_id INT,     IN p_codigo_pago VARCHAR(50),     IN p_monto DECIMAL(8,2),     IN p_fecha_pago DATETIME,     IN p_estado VARCHAR(20) | `PagoDAOImpl.java` |
| `eliminar_pago` | IN p_id INT | `PagoDAOImpl.java` |
| `listar_entradas` | Sin parámetros | `EntradaDAOImpl.java` |
| `buscar_entrada_por_id` | IN p_id INT | `EntradaDAOImpl.java` |
| `buscar_entrada_por_reserva` | IN p_reserva_id INT | `EntradaDAOImpl.java` |
| `buscar_entrada_por_qr` | IN p_codigo_qr VARCHAR(100) | `EntradaDAOImpl.java` |
| `insertar_entrada` | IN p_reserva_id INT,     IN p_butaca_id INT,     IN p_fecha_emision DATETIME,     IN p_codigo_qr VARCHAR(100),     IN p_precio_final DECIMAL(8,2),     IN p_estado VARCHAR(20),     OUT p_id INT | `EntradaDAOImpl.java` |
| `modificar_entrada` | IN p_id INT,     IN p_estado VARCHAR(20),     IN p_precio_final DECIMAL(8,2) | `EntradaDAOImpl.java` |
| `eliminar_entrada` | IN p_id INT | `EntradaDAOImpl.java` |
