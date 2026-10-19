# Catálogo de procedimientos almacenados — LAB06

**Fuente verificada:** declaraciones `CREATE PROCEDURE` extraídas de [`procedimientos.sql`](../entrefunciones/sql/procedimientos.sql) del ZIP entregado para LAB06. **No se ha conectado a MySQL ni ejecutado los SP**.

> **Regla:** no editar esta tabla sin revisar el SQL correspondiente. Es un inventario de lectura, no evidencia de que cada SP funcione.

## Resumen

| Área | SP declarados |
| --- | ---: |
| Usuarios · administrador | 8 |
| Usuarios · cliente | 7 |
| Sedes | 6 |
| Salas | 6 |
| Recursos | 5 |
| Eventos | 11 |
| Funciones | 8 |
| Reservas | 8 |
| Pagos | 7 |
| Entradas | 7 |
| **Total** | **73** |

**Correspondencias estáticas:** 72 nombres de SP aparecen en alguno de los archivos `*DAOImpl.java`. `buscar_sede_por_razon_social` está declarado en el SQL pero no se encontró por nombre en los DAO de esta entrega. Una referencia textual **no garantiza que el código invoque correctamente el SP**.

## Usuarios · administrador

| Nombre de SP | Operación | DAO que lo menciona |
| --- | --- | --- |
| `listar_administradores` | Consulta/listado | AdministradorDAOImpl.java |
| `buscar_administrador_por_id` | Consulta/búsqueda | AdministradorDAOImpl.java |
| `buscar_administrador_por_correo` | Consulta/búsqueda | AdministradorDAOImpl.java |
| `buscar_administrador_por_dni` | Consulta/búsqueda | AdministradorDAOImpl.java |
| `buscar_administrador_por_codigo` | Consulta/búsqueda | AdministradorDAOImpl.java |
| `insertar_administrador` | Inserción | AdministradorDAOImpl.java |
| `modificar_administrador` | Modificación | AdministradorDAOImpl.java |
| `eliminar_administrador` | Eliminación | AdministradorDAOImpl.java |

## Usuarios · cliente

| Nombre de SP | Operación | DAO que lo menciona |
| --- | --- | --- |
| `listar_clientes` | Consulta/listado | ClienteDAOImpl.java |
| `buscar_cliente_por_id` | Consulta/búsqueda | ClienteDAOImpl.java |
| `buscar_cliente_por_correo` | Consulta/búsqueda | ClienteDAOImpl.java |
| `buscar_cliente_por_dni` | Consulta/búsqueda | ClienteDAOImpl.java |
| `insertar_cliente` | Inserción | ClienteDAOImpl.java |
| `modificar_cliente` | Modificación | ClienteDAOImpl.java |
| `eliminar_cliente` | Eliminación | ClienteDAOImpl.java |

## Sedes

| Nombre de SP | Operación | DAO que lo menciona |
| --- | --- | --- |
| `listar_sedes` | Consulta/listado | SedeDAOImpl.java |
| `buscar_sede_por_id` | Consulta/búsqueda | SedeDAOImpl.java |
| `buscar_sede_por_razon_social` | Consulta/búsqueda | No identificado por nombre |
| `insertar_sede` | Inserción | SedeDAOImpl.java |
| `modificar_sede` | Modificación | SedeDAOImpl.java |
| `eliminar_sede` | Eliminación | SedeDAOImpl.java |

## Salas

| Nombre de SP | Operación | DAO que lo menciona |
| --- | --- | --- |
| `listar_salas` | Consulta/listado | SalaDAOImpl.java |
| `buscar_sala_por_id` | Consulta/búsqueda | SalaDAOImpl.java |
| `listar_salas_por_sede` | Consulta/listado | SalaDAOImpl.java |
| `insertar_sala` | Inserción | SalaDAOImpl.java |
| `modificar_sala` | Modificación | SalaDAOImpl.java |
| `eliminar_sala` | Eliminación | SalaDAOImpl.java |

## Recursos

| Nombre de SP | Operación | DAO que lo menciona |
| --- | --- | --- |
| `listar_recursos` | Consulta/listado | RecursoDAOImpl.java |
| `buscar_recurso_por_id` | Consulta/búsqueda | RecursoDAOImpl.java |
| `insertar_recurso` | Inserción | RecursoDAOImpl.java |
| `modificar_recurso` | Modificación | RecursoDAOImpl.java |
| `eliminar_recurso` | Eliminación | RecursoDAOImpl.java |

## Eventos

| Nombre de SP | Operación | DAO que lo menciona |
| --- | --- | --- |
| `listar_eventos` | Consulta/listado | EventoDAOImpl.java |
| `buscar_evento_por_id` | Consulta/búsqueda | EventoDAOImpl.java |
| `insertar_evento` | Inserción | EventoDAOImpl.java |
| `modificar_evento` | Modificación | EventoDAOImpl.java |
| `eliminar_evento` | Eliminación | EventoDAOImpl.java |
| `insertar_evento_pelicula` | Inserción | EventoDAOImpl.java |
| `modificar_evento_pelicula` | Modificación | EventoDAOImpl.java |
| `insertar_evento_obra_teatro` | Inserción | EventoDAOImpl.java |
| `modificar_evento_obra_teatro` | Modificación | EventoDAOImpl.java |
| `insertar_evento_actividad_cultural` | Inserción | EventoDAOImpl.java |
| `modificar_evento_actividad_cultural` | Modificación | EventoDAOImpl.java |

## Funciones

| Nombre de SP | Operación | DAO que lo menciona |
| --- | --- | --- |
| `listar_funciones` | Consulta/listado | FuncionDAOImpl.java |
| `listar_funciones_por_evento` | Consulta/listado | FuncionDAOImpl.java |
| `listar_funciones_por_sala` | Consulta/listado | FuncionDAOImpl.java |
| `buscar_funcion_por_id` | Consulta/búsqueda | FuncionDAOImpl.java |
| `insertar_funcion` | Inserción | FuncionDAOImpl.java |
| `modificar_funcion` | Modificación | FuncionDAOImpl.java |
| `cancelar_funcion` | Cancelación | FuncionDAOImpl.java |
| `cancelar_funciones_por_evento` | Cancelación | FuncionDAOImpl.java |

## Reservas

| Nombre de SP | Operación | DAO que lo menciona |
| --- | --- | --- |
| `listar_reservas` | Consulta/listado | ReservaDAOImpl.java |
| `listar_reservas_por_cliente` | Consulta/listado | ReservaDAOImpl.java |
| `listar_reservas_por_funcion` | Consulta/listado | ReservaDAOImpl.java |
| `buscar_reserva_por_id` | Consulta/búsqueda | ReservaDAOImpl.java |
| `insertar_reserva` | Inserción | ReservaDAOImpl.java |
| `insertar_reserva_butaca` | Inserción | ReservaDAOImpl.java |
| `modificar_reserva` | Modificación | ReservaDAOImpl.java |
| `eliminar_reserva` | Eliminación | ReservaDAOImpl.java |

## Pagos

| Nombre de SP | Operación | DAO que lo menciona |
| --- | --- | --- |
| `listar_pagos` | Consulta/listado | PagoDAOImpl.java |
| `buscar_pago_por_id` | Consulta/búsqueda | PagoDAOImpl.java |
| `buscar_pago_por_reserva` | Consulta/búsqueda | PagoDAOImpl.java |
| `buscar_pago_por_codigo` | Consulta/búsqueda | PagoDAOImpl.java |
| `insertar_pago` | Inserción | PagoDAOImpl.java |
| `modificar_pago` | Modificación | PagoDAOImpl.java |
| `eliminar_pago` | Eliminación | PagoDAOImpl.java |

## Entradas

| Nombre de SP | Operación | DAO que lo menciona |
| --- | --- | --- |
| `listar_entradas` | Consulta/listado | EntradaDAOImpl.java |
| `buscar_entrada_por_id` | Consulta/búsqueda | EntradaDAOImpl.java |
| `buscar_entrada_por_reserva` | Consulta/búsqueda | EntradaDAOImpl.java |
| `buscar_entrada_por_qr` | Consulta/búsqueda | EntradaDAOImpl.java |
| `insertar_entrada` | Inserción | EntradaDAOImpl.java |
| `modificar_entrada` | Modificación | EntradaDAOImpl.java |
| `eliminar_entrada` | Eliminación | EntradaDAOImpl.java |

## Siguiente auditoría

El siguiente paso es comprobar, por cada procedimiento **utilizado en un flujo**, sus parámetros IN/OUT, tablas leídas o escritas, manejo de resultados y transacción. Priorizar `insertar_sede`, `insertar_sala`, `insertar_reserva`, `insertar_pago` e `insertar_entrada`. No completar con supuestos: abrir DAO y SQL y registrar la prueba ejecutada.
