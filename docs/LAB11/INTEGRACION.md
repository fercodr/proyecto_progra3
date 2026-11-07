# Integración de las correcciones del LAB11

## Base de datos

En una instalación nueva, abrir y ejecutar en Workbench primero `Backend/sql/entrefunciones-mysql.sql` y luego `Backend/sql/procedimientos.sql`. Los datos de `datos-demo.sql` son opcionales; sus funciones quedan en fechas futuras relativas al día de carga.

En una BD existente, conservar un respaldo y probar primero en una copia. Aplicar `migracion-lab11.sql` y luego `procedimientos.sql`. No ejecutar el esquema de instalación sobre las tablas existentes. La migración retira las tablas del módulo descartado y conserva las ventas; añade el precio unitario acordado y recupera la tabla puente cuando solo existe con el nombre antiguo. Si encuentra ambas tablas puente, se detiene para que el equipo concilie sus datos. Una instalación ya actualizada no necesita repetir el esquema.

Los procedimientos de programación y los DAO de ventas se usan desde el BL transaccional. Los bloqueos de fila se toman dentro de esa transacción. No implementar compras llamando solo al DAO: se omitirían validaciones de concurrencia y disponibilidad.

## Backend Java

Abrir `Backend/pom.xml` en IntelliJ y cargar Maven con JDK 21 o superior. El módulo `entrefunciones-rest` genera `entrefunciones-rest.war`; usar el objetivo Package en la ventana Maven y desplegarlo en GlassFish. El servidor debe incluir la plataforma Jakarta EE completa para ejecutar el temporizador EJB.

Configurar estas variables en la ejecución del servidor o como propiedades del sistema Java:

| Variable | Uso |
|---|---|
| `DB_HOST` | Servidor MySQL; predeterminado localhost |
| `DB_PORT` | Puerto; predeterminado 3306 |
| `DB_NAME` | Esquema; predeterminado entrefunciones |
| `DB_USER` | Usuario de BD |
| `DB_PASSWORD` | Contraseña de BD |
| `ENTREFUNCIONES_API_KEY` | Clave privada compartida exclusivamente con el servidor C# |

Se mantiene el soporte de `db.properties` como alternativa. La conexión carga explícitamente el driver, consulta metadatos de procedimientos en information_schema y fija la sesión a UTC−05 para que fechas y vencimiento sean consistentes.

## Frontend C#

Abrir el proyecto en Visual Studio. Configurar `Rest:BaseUrl` y `Rest:ApiKey` en la configuración privada del servidor; por variables de entorno se llaman `Rest__BaseUrl` y `Rest__ApiKey`. La clave debe coincidir con la de Java. La URL predeterminada es `http://localhost:8080/entrefunciones-rest/v1/api/`.

El cliente HTTP envía la identidad tomada de la sesión C# y la clave entre servidores. Java verifica que esa cuenta exista y esté activa, y consulta su rol en BD. Nunca colocar esta clave en JavaScript, archivos públicos o formularios del navegador. Usar HTTPS cuando los servidores se comuniquen fuera del entorno local.

El login actual todavía utiliza las cuentas demo y las identidades 1 y 2. El responsable de cuentas debe sustituirlo por autenticación real y conservar el ID persistido en `ClaimTypes.NameIdentifier`. Las correcciones no sustituyen ese trabajo.

## Contratos entregados

Todas las rutas siguientes se encuentran bajo `v1/api`:

| Método y ruta | Resultado |
|---|---|
| GET `funciones/{id}/disponibilidad` | Función, precio, modalidad, cupos y butacas reales |
| POST `reservas` | Crea una reserva del cliente de la sesión; recibe funcionId, butacaIds y cantidad |
| GET `reservas/{id}` | Reserva propia o consulta administrativa |
| GET `reservas` | Historial propio o listado completo para administrador |
| DELETE `reservas/{id}` | Cancela una reserva activa y libera cupos |
| POST `reservas/{id}/pago` | Genera o recupera el pago de la reserva |
| POST `pagos/{id}/registrar` | Registra un pago simulado pendiente de revisión |
| GET `pagos` | Listado administrativo |
| POST `pagos/{id}/validar` | Confirma y emite entradas en una sola transacción; repetir no duplica entradas |
| POST `pagos/{id}/rechazar` | Rechaza y libera la reserva activa |

Los errores devuelven JSON con el campo `mensaje`. Los importes, la identidad del cliente y la expiración se resuelven en el servidor. No se transmiten ni almacenan los campos de tarjeta del formulario de demostración.

Las páginas Selección de butacas, Reserva y pago y Ventas ya consumen estos contratos. Cartelera y Detalle conservan sus listas demo: el integrante responsable debe sustituirlas por funciones reales y enlazar con sus IDs de BD. Para probar directamente la compra tras cargar los datos opcionales, usar la ruta `/funcion/1/butacas` para butacas numeradas o `/funcion/6/butacas` para aforo general, con una sesión cliente. Los IDs 101–106 del detalle demo no equivalen a los IDs del SQL.

El vencimiento se ejecuta cada 30 segundos desde el servidor Java y también se comprueba al operar. Una reserva vence exactamente al alcanzar su fecha de expiración. El plazo es de 15 minutos o hasta el inicio de la función, lo que ocurra primero. Registrar el pago no amplía el plazo para su validación administrativa.

## Decisiones y pendientes del equipo

- Una función usa una fecha única: el horario termina el mismo día y se rechaza el cruce de medianoche. Los horarios contiguos se permiten.
- Con reservas activas o confirmadas no se cambia sala, fecha ni horario de una función.
- Recursos y Bloqueos se retiraron del código vigente y la propuesta del informe; las entregas históricas se conservan.
- El grupo conserva el diseño de las pantallas, confirmaciones, avisos y etapas visuales de compra.
- Continúan las integraciones de cuentas, cartelera/detalle, espacios, programación administrativa, Mis compras, perfil y M6 según sus responsables. No se acredita el 60% solo por estas correcciones.
- El catálogo propuesto tiene 17 RF y conserva los códigos originales. El retiro de RF-M2-02 y los ajustes de RF-M3-01/02 deben presentarse al JP; el catálogo evaluable sigue siendo el que este apruebe.
- El script histórico `entrega.bat` aún apunta a la carpeta anterior `Codigo`; este ZIP ya incluye Backend, Frontend y documentos sin depender de ese script.
