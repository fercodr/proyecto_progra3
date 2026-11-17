# Catálogo funcional de EntreFunciones

- **Fecha:** 2026-11-15
- **Versión:** 0.2 (catálogo incorporado para la preparación de LAB12)
- **Estado de conformidad de la JP:** Pendiente de confirmar los cambios de alcance. Esta versión no acredita aprobación de la JP.
- **Cantidad de requerimientos funcionales:** 17.

## Alcance y actores

EntreFunciones administra la publicación de funciones y la gestión de reservas, pagos y entradas. Los administradores registran los datos y cambios comunicados por las sedes. Las sedes no disponen de un perfil administrativo propio y reciben información mediante los reportes generados por la plataforma.

El alcance de esta propuesta no incluye la gestión de equipos de las sedes ni el control presencial de acceso a las funciones. No se registra asistencia física ni se incorpora una funcionalidad nueva de consulta pública de autenticidad. Se conservan los bloqueos temporales de salas.

## Requerimientos

Las descripciones siguen la estructura «El sistema permitirá a <actor o actores> <acción>».

| Módulo | Código | Nombre | Descripción | Prioridad |
|---|---|---|---|---|
| M1 | RF-M1-01 | Gestión de cuenta y autenticación | El sistema permitirá a las personas registrar una cuenta con nombre, correo y contraseña, y a los usuarios registrados consultar y modificar sus propios datos, iniciar sesión con sus credenciales y cerrar sesión. | Alta |
| M1 | RF-M1-02 | Control de acceso por roles | El sistema permitirá a los clientes y administradores acceder únicamente a las funcionalidades correspondientes a su rol, restringiendo las operaciones administrativas a los usuarios autorizados. Las sedes no contarán con un rol de administración propio. | Media |
| M1 | RF-M1-03 | Recuperación de contraseña | El sistema permitirá a los usuarios registrados solicitar el restablecimiento de su contraseña mediante su correo registrado y establecer una nueva contraseña después de verificar la solicitud de recuperación. | Media |
| M2 | RF-M2-01 | Gestión de sedes y salas | El sistema permitirá al administrador registrar, consultar, modificar y desactivar sedes y salas con la información comunicada por las sedes, incluyendo su ubicación, distrito y las características de accesibilidad, así como el aforo de cada sala. La desactivación conservará la información histórica asociada. | Alta |
| M2 | RF-M2-02 | Bloqueo temporal de salas | El sistema permitirá al administrador registrar, consultar, modificar y desactivar bloqueos temporales de una sala, indicando el inicio y fin de su indisponibilidad conforme a la información comunicada por la sede. Los bloqueos vigentes se considerarán al consultar la disponibilidad y confirmar funciones. | Alta |
| M2 | RF-M2-03 | Gestión de butacas | El sistema permitirá al administrador registrar, consultar, modificar y desactivar las butacas de una sala, indicando su fila, columna y tipo, para conformar el mapa de asientos utilizado en las reservas. Las modificaciones conservarán la información de las reservas y entradas previamente emitidas. | Alta |
| M3 | RF-M3-01 | Programación de funciones | El sistema permitirá al administrador registrar, consultar y modificar funciones, incorporando en un mismo flujo los datos de su contenido —película, obra de teatro o actividad cultural—, como título, duración, género, temática o elenco según corresponda, y sus datos de programación: sede, sala, fecha, horario, precio y público objetivo. | Alta |
| M3 | RF-M3-02 | Consulta y validación de disponibilidad | El sistema permitirá al administrador consultar la disponibilidad de las salas antes de programar una función y confirmar únicamente funciones cuyo horario no se superponga con otra función confirmada ni con un bloqueo vigente de la misma sala. | Alta |
| M3 | RF-M3-03 | Publicación y cancelación de funciones | El sistema permitirá al administrador confirmar y publicar funciones programadas, así como cancelarlas. Solo las funciones confirmadas podrán ofrecerse en cartelera para nuevas reservas; las canceladas dejarán de admitirlas y conservarán su información histórica. | Alta |
| M4 | RF-M4-01 | Consulta de cartelera | El sistema permitirá al público consultar el detalle de las funciones publicadas y filtrarlas por fecha, sede, distrito, tipo de actividad, temática, precio y accesibilidad. | Alta |
| M4 | RF-M4-02 | Selección de butacas | El sistema permitirá al cliente visualizar el mapa de butacas de una función, identificar las disponibles, ocupadas o retenidas temporalmente por una reserva, y seleccionar una o más butacas disponibles para iniciar su reserva. Una misma butaca no podrá asignarse simultáneamente a dos reservas vigentes de la misma función. | Alta |
| M5 | RF-M5-01 | Reserva temporal de aforo | El sistema permitirá al cliente registrar y consultar una reserva temporal de cupos de una función, asociada a las butacas seleccionadas cuando corresponda, conocer su vencimiento y cancelarla. Los cupos y butacas se liberarán automáticamente cuando la reserva se cancele o venza sin completar la confirmación de la operación. | Alta |
| M5 | RF-M5-02 | Registro y validación de pagos | El sistema permitirá al cliente consultar el código de pago generado para su reserva y registrar el pago realizado. Asimismo, permitirá al administrador buscar el pago por su código, consultar los datos registrados y validarlo para confirmar la operación. | Alta |
| M5 | RF-M5-03 | Gestión de entradas | El sistema permitirá al cliente obtener una entrada con identificador único y código QR por cada cupo adquirido, una vez confirmado el pago o una reserva gratuita, consultar su historial de entradas y cancelarlas conforme a las condiciones de cancelación aplicables. Cada entrada estará asociada a su reserva y función, y tendrá estado vigente o cancelada. La cancelación liberará el cupo y la butaca correspondiente. | Alta |
| M6 | RF-M6-02 | Reporte de ocupación por entradas emitidas | El sistema permitirá al administrador generar un reporte filtrado por sede, sala y periodo que muestre, para cada función, su aforo, la cantidad de entradas vigentes emitidas y el porcentaje de ocupación, calculado como **entradas vigentes emitidas ÷ aforo de la función × 100**. El conteo incluirá entradas pagadas y gratuitas, y excluirá entradas canceladas y reservas sin entradas emitidas. Este indicador representará aforo comprometido mediante entradas, no asistencia física. | Alta |
| M6 | RF-M6-03 | Reporte de ingresos económicos y cancelaciones | El sistema permitirá al administrador generar un reporte filtrado por sede, periodo y tipo de actividad —película, obra de teatro o actividad cultural— que muestre el importe de pagos confirmados, la cantidad de entradas emitidas y la cantidad de entradas canceladas. Los pagos pendientes no se contabilizarán como ingresos confirmados y las entradas gratuitas no generarán ingresos monetarios. | Alta |
| M6 | RF-M6-04 | Gestión de incidencias operativas | El sistema permitirá al administrador registrar y consultar incidencias relacionadas con una función, a partir de comunicaciones de las sedes o los clientes, indicando la función asociada, descripción, fecha, responsable y estado de atención. Asimismo, permitirá actualizar la descripción, el responsable y el estado para dar seguimiento a su atención. | Media |

## Registro de cambios

| Fecha | Versión | Elemento | Cambio registrado | Conformidad de la JP |
|---|---|---|---|---|
| 2026-11-15 | 0.1 | Plantilla inicial | Creación de la plantilla; todavía no contenía requerimientos. | Pendiente |
| 2026-11-15 | 0.2 | Catálogo completo | Incorporación de los 17 RF suministrados, con sus códigos y prioridades. Se agregó la cabecera de la tabla y se eliminaron únicamente las repeticiones idénticas de RF-M1-01 y RF-M1-02 del texto pegado. | Pendiente respecto de los cambios de alcance |
| 2026-11-15 | 0.2 | Actores y sedes | La actualización de los datos comunicados por las sedes corresponde a los administradores de EntreFunciones; no se incorpora un perfil administrativo para las sedes. | Pendiente |
| 2026-11-15 | 0.2 | RF-M2-02, RF-M3-01 y RF-M3-02 | Retiro de la gestión y comprobación de equipos o recursos. RF-M2-02 se limita al bloqueo temporal de salas, conservado en las validaciones de disponibilidad. | Pendiente |
| 2026-11-15 | 0.2 | RF-M6-01 | Retiro de «Validación y registro de ingreso» de esta propuesta. Su código se conserva en este historial y no se reutiliza; los demás RF mantienen su numeración. | Pendiente |
| 2026-11-15 | 0.2 | RF-M5-03 | Se conservan emisión con identificador y QR, historial y cancelación con liberación del cupo y la butaca. Los estados de la propuesta son vigente y cancelada; se retira el estado utilizada. No se incorpora otra funcionalidad para sustituir el control de ingreso. | Pendiente |
| 2026-11-15 | 0.2 | RF-M6-02 | La ocupación se define mediante entradas vigentes emitidas respecto del aforo, incluyendo gratuitas y excluyendo canceladas y reservas sin emisión. No representa asistencia física. | Pendiente |
| 2026-11-15 | 0.2 | RF-M6-03 | Se conserva el segundo reporte como ingresos económicos y cancelaciones, con filtros por sede, periodo y tipo de actividad. Se retira la asistencia física como indicador. | Pendiente |
| 2026-11-15 | 0.2 | RF-M6-04 | Se explicita el origen de las incidencias en comunicaciones de sedes o clientes y la actualización de descripción, responsable y estado para su seguimiento. No se presupone detección automática. | Pendiente |

## Decisiones pendientes

- Confirmar con la JP los cambios de alcance registrados y la cantidad de RF exigida al equipo. La propuesta contiene 17 RF; no se ha añadido otro para compensar el retiro de RF-M6-01.
- Precisar las condiciones de cancelación mencionadas en RF-M5-03 y el tratamiento económico de las cancelaciones. No se incorporan automáticamente plazos, penalidades ni gestión de reembolsos.
- Acordar qué fecha determina el periodo de cada reporte para que sus filtros tengan una interpretación uniforme.

## Uso como referencia del proyecto

Este archivo reúne el catálogo proporcionado para orientar el trabajo de la TA. Los informes históricos y las auditorías anteriores pueden corresponder a otro alcance; sus conclusiones deberán contrastarse con esta versión.

La existencia de un RF en este documento no demuestra que esté implementado o probado. El estado de implementación y las verificaciones se documentan por separado en `ESTADO.md`. Esta actualización documental no elimina código ni modifica estados o registros existentes en la base de datos.
