# Estado de EntreFunciones

## Flujo de compra del cliente: entradas con QR local (2026-11-16, bloque del Integrante 4)

### Cambios realizados (solo frontend C#, sin commit)

- **RF-M5-03 — QR:** nuevo componente `Components/Shared/CodigoQr.razor`, que dibuja el QR en el propio servidor con el paquete NuGet `QRCoder` 1.6.0 (agregado a `EntreFunciones.csproj`). Se retiró el uso de `api.qrserver.com` en `MisCompras.razor`: el código de la entrada ya no se envía a un servicio externo ni depende de internet.
- **RF-M5-03 — historial de entradas:** `MisCompras.razor` tiene dos pestañas, Reservas y Entradas. La pestaña Entradas lista cada entrada con identificador (`ENT-000000`), función, fecha y hora, butaca, precio y estado, con las acciones Ver QR y Cancelar. La cancelación individual ahora pide confirmación previa. Nuevos `EntradaClienteViewModel` y métodos de entradas en `CompraClienteMapper`.
- **RF-M5-01 / RF-M5-02:** `MisCompras.razor` ya no queda en error completo cuando falla la consulta del pago de una sola reserva, y no consulta reservas ya vencidas. `ReservaPago.razor` muestra el vencimiento y el código de pago desde la etapa Reserva, y un fallo al generar el pago se muestra sobre la reserva.
- **Reservas gratuitas:** `SeleccionButacas.razor` y `ReservaPago.razor` usan textos propios para funciones de precio cero (sin pago ni plazo de 15 minutos); la confirmación muestra las entradas emitidas con su QR.
- No se modificó el backend Java, la base de datos, el catálogo ni la pantalla administrativa de Ventas.

### Verificación

- **Implementación por código, no probada.** Los cambios se escribieron sin compilar: el entorno de edición no disponía de .NET. No se ejecutó `dotnet build`, ni el recorrido C# → REST → MySQL.
- **Pendiente inmediato:** `dotnet restore` (descarga `QRCoder`) y `dotnet build` de `Frontend/EntreFunciones`; luego recorrer una compra pagada (validada por el administrador), una reserva gratuita y la cancelación de una entrada, comprobando que el QR se dibuja y que la butaca se libera.
- **Cómo revertir:** `git checkout -- Frontend/EntreFunciones CONTEXTO-TA/ESTADO.md` y borrar `Components/Shared/CodigoQr.razor` y `ViewModels/EntradaClienteViewModel.cs`.

## Paso 6 — diagnóstico y corrección del login (2026-11-15, 22:45 hora de Lima)

### Causa confirmada y correcciones

- El evento de IIS demostró que `POST /cuenta/login` alcanzaba el REST y recibía HTTP 500. El `server.log` remoto confirmó la causa: `ENTREFUNCIONES_JWT_SECRET` no estaba configurada con al menos 32 bytes; no existe evidencia de un fallo de credenciales.
- Se mantiene la validación mínima de 32 bytes. `JwtServicio` carga la clave al emitir o validar el JWT, y `CuentasResource` convierte una configuración JWT inválida en HTTP 503 JSON sin exponer la excepción al cliente. La prueba focal con un secreto deliberadamente corto devolvió 503 `application/json` con un mensaje controlado, no 500 HTML.
- El frontend apunta por defecto a `http://ec2-3-89-249-99.compute-1.amazonaws.com:8080/entrefunciones-rest/v1/api/`. .NET permite sobrescribirlo externamente mediante `Rest__BaseUrl`; no hay `appsettings.Production.json` ni una variable equivalente incluida en el `web.config` publicado.
- `HttpUtils` conserva el estado HTTP y registra de forma segura el endpoint sin parámetros y el tipo de contenido. `SesionServiceImpl` controla fallos REST, respuestas de sesión incompletas y fallos de creación de cookie; redirige a mensajes de login diferenciados en vez de dejar una excepción sin controlar. No registra correo, contraseña, token ni cookie.

### Cuenta administradora adicional de prueba

- `admin.katita@entrefunciones.test` quedó creada y comprobada exclusivamente en `entrefunciones_lab12_test` el 2026-11-15 a las 22:38:30 -05:00.
- La contraseña se eligió mediante una entrada local oculta, se mantuvo solo en memoria y no se escribió en archivos. La creación utilizó `AdministradorBLImpl.insert`, que aplica el mecanismo PBKDF2 del proyecto y el procedimiento transaccional existente.
- `ProvisionAdminLab12Test`: 1 prueba aprobada, 0 fallos y 0 errores. Verificó esquema real con `SELECT DATABASE()`, cuenta activa, hash distinto del texto plano, verificación del hash, autenticación como administrador y unicidad del correo. El mismo proceso comprobó además un login HTTP 200 contra el GlassFish aislado conectado al esquema de pruebas. Las cuentas existentes no se eliminaron ni modificaron.

### Pruebas focales ejecutadas

- **Negocio/JWT + MySQL:** `LoginRestIntegrationTest`, 2 aprobadas, 0 fallos; esquema exigido y comprobado: `entrefunciones_lab12_test`.
- **REST HTTP + MySQL:** login válido 200 con JWT, acceso a un recurso administrativo 200 y contraseña inválida 401 JSON. La cuenta adicional aprobó también su login REST 200.
- **C# → REST Java → MySQL:** formulario con antiforgery, login válido, redirección a `/admin` y cookie creada; credencial inválida volvió a `/login?error=true` sin cookie. La repetición posterior al `publish` volvió a aprobar el login y la cookie.
- **Error REST controlado desde C#:** un REST local simulado devolvió 500 `text/html`; el frontend terminó en `/login?servicioNoDisponible=true`, mostró el estado controlado y registró solo endpoint, estado y tipo de contenido.
- **JWT mal configurado:** el backend aislado, siempre conectado a `entrefunciones_lab12_test`, devolvió 503 JSON ante una clave corta. El GlassFish temporal se detuvo al finalizar y el dominio preexistente no fue modificado.
- No se ejecutaron estas pruebas contra `entrefunciones`, `DB2` ni el backend publicado. No se modificó AWS.

### Artefactos y despliegue pendiente

- **WAR local vigente:** `Backend/entrefunciones-rest/target/entrefunciones-rest.war`, 14 274 461 bytes, 2026-11-15 22:26:36 -05:00, SHA-256 `7257258DE95E3EB30D8796A65F5DADC86D47DE07F334A51BB1A9DEC5140419B0`. El único archivo Java posterior es la prueba `ProvisionAdminLab12Test.java`; no hay fuente productiva posterior que obligue a regenerar el WAR.
- **Frontend IIS/.NET 10 regenerado:** `Frontend/EntreFunciones/bin/Release/net10.0/publish/`; `EntreFunciones.dll`, 574 976 bytes, 2026-11-15 22:42:52 -05:00, SHA-256 `979436242619A4481026870DE181C3F5EC88C6F2CFBB1BB7944B61AB3B651B94`.
- Para corregir el servidor publicado se requiere configurar externamente `ENTREFUNCIONES_JWT_SECRET` y reemplazar el WAR por el artefacto local corregido. Para IIS se requiere reemplazar el contenido por el `publish` regenerado o aplicar las mismas fuentes/configuración. Estas acciones no se ejecutaron.
- Sigue pendiente comprobar qué WAR y qué esquema están efectivamente desplegados en el GlassFish remoto. El `server.log` confirmó la ausencia de la clave JWT, pero no identifica por sí solo la versión del WAR ni el valor efectivo de `DB_NAME`.

## Cierre reanudado de LAB12 (2026-11-15, 20:55 hora de Lima)

### Ejecución recuperada y diagnóstico

- La ejecución Maven/Surefire que quedó iniciada terminó; no se lanzó una segunda copia de la misma suite. Ejecutó los 16 casos de `IntegracionLab11Test` contra `entrefunciones_lab12_test` en 564.5 s: 14 aprobados, 2 fallidos y 0 omitidos.
- Los dos fallos fueron `horariosRechazanCrucesYPermitenLimitesDiasYAutoedicion` y `programacionConcurrenteSerializaLaSala`. Ambos tests construían funciones `PROGRAMADA`, pero esperaban la exclusividad que RF-M3-02 exige al confirmar. No se atribuye el resultado a latencia.
- Se corrigió exclusivamente la precondición de esos tests para usar funciones `CONFIRMADA`. La ejecución focal posterior aprobó los 2 casos, con 0 fallos, contra el mismo esquema. No se repitió la suite completa, por lo que no se declara una corrida consolidada de 16/16.
- La guardia continúa exigiendo simultáneamente `EF_TEST_DB=1` y `DB_NAME=entrefunciones_lab12_test`. El bootstrap exige además `EF_BOOTSTRAP_TEST_DB=1` y rechaza cualquier esquema distinto.

### Tres pruebas integradas focales conservadas

Estas pruebas se ejecutaron y aprobaron individualmente mediante negocio/DAO + MySQL. No incluyen transporte HTTP REST ni la pantalla C#.

| RF | Caso Surefire individual | Resultado | Evidencia temporal |
|---|---|---|---|
| RF-M2-03 | `editarYDesactivarButacaConservaReservaYEntradaHistoricas` | 1 aprobada, 0 fallos; conserva `reserva_butaca`, entrada, id de butaca y desactivación lógica. | 44.46 s; finalizó 20:35:42. |
| RF-M3-01 | `contenidoYProgramacionSePersistenJuntos` | 1 aprobada, 0 fallos; contenido, temática y función quedaron relacionados en una operación de negocio. | 13.22 s; finalizó 20:36:09. |
| RF-M5-02 | `pagoSeBuscaPorCodigoHastaLaConsultaDeDatos` | 1 aprobada, 0 fallos; el código normalizado recuperó pago, reserva y cliente desde MySQL. | 29.16 s; finalizó 20:34:42. |

La corrida completa posterior también aprobó estos tres casos. Los otros resultados con evidencia fueron 11 casos aprobados y los 2 fallos de precondición descritos arriba; después, esos 2 casos aprobaron focalmente. No se suman casos sin reporte.

### Cambios cerrados en los tres RF priorizados

- **RF-M2-03:** modificar una sala ya no elimina y recrea sus butacas. `deleteButacas` y la baja individual desactivan lógicamente; la edición conserva el id y no permite mover una butaca existente a otra sala. Así se mantienen las referencias históricas de reservas y entradas.
- **RF-M3-01:** `GestionarCatalogo.razor` permite registrar o editar contenido y una programación inicial/seleccionada en el mismo flujo. El `EventoDTO` transporta sus funciones y el backend las persiste en una sola transacción de negocio.
- **RF-M5-02:** la pantalla de ventas realiza búsqueda exacta por código mediante `CompraService`, `GET /pagos/codigo/{codigoPago}`, `PagoBLImpl` y la consulta DAO. El DTO REST devuelve también el cliente asociado.

### Estado actual de los 17 RF

`Implementado por código` significa que la cadena está presente en fuentes; no equivale a un recorrido completo probado. El balance es 16 de 17 implementados por código (94.1 %) y 1 pendiente. No se declara 90 % funcionando de extremo a extremo porque no se ejecutó C# → REST HTTP → MySQL.

| RF | Estado por código | Evidencia disponible | Prueba funcional pendiente |
|---|---|---|---|
| RF-M1-01 | Implementado | Registro, autenticación y acceso a datos propios presentes. | Alta, login, consulta y edición propia desde C# hasta MySQL. |
| RF-M1-02 | Implementado | Guardas REST de administrador/propietario y filtrado público presentes. | Casos HTTP 401/403 y recorrido de ambos roles. |
| RF-M1-03 | Pendiente | El cambio directo existente no constituye recuperación verificada por correo. | Diseñar e implementar solicitud, verificación y nueva contraseña. |
| RF-M2-01 | Implementado por código | CRUD lógico, distrito, accesibilidad y conservación de asociaciones presentes. | CRUD completo desde C# → REST → MySQL. |
| RF-M2-02 | Implementado por código | CRUD de bloqueos y validación de solapamiento en negocio/procedimientos; 2 pruebas unitarias aprobadas. | CRUD REST y conflicto real con bloqueo desde C#. |
| RF-M2-03 | Implementado por código y probado con MySQL | Prueba focal individual aprobada; 10 pruebas unitarias de espacios aprobadas. | Recorrido C# → REST y verificación visual del historial. |
| RF-M3-01 | Implementado por código y probado con MySQL | Prueba focal individual aprobada para contenido + programación transaccional. | Recorrido C# → REST para los tres tipos de actividad y edición. |
| RF-M3-02 | Implementado por código y probado con MySQL | Dos casos focales aprobados para cruce confirmado y concurrencia; propuestas `PROGRAMADA` pueden coexistir según catálogo. | Consulta previa y confirmación mediante REST/C#. |
| RF-M3-03 | Implementado por código y probado en negocio/DAO + MySQL | Cancelación persistente aprobada en la corrida integrada; reserva exige función confirmada. | Publicación/cancelación por HTTP y rechazo de petición directa. |
| RF-M4-01 | Implementado por código | Filtros combinados de cartelera presentes hasta C#. | Combinaciones y detalle público desde C# con MySQL. |
| RF-M4-02 | Implementado por código y probado en negocio/DAO + MySQL | Contención de la misma butaca aprobada en la corrida integrada. | Estados visuales y dos clientes desde C#; transporte HTTP pendiente. |
| RF-M5-01 | Implementado por código y probado en negocio/DAO + MySQL | Casos de vencimiento, cancelación e idempotencia aprobados. | Temporizador y liberación observados desde C# y REST. |
| RF-M5-02 | Implementado por código y probado con MySQL | Prueba focal individual aprobada para la búsqueda exacta; endpoint y servicio C# compilados. | Llamada HTTP real y búsqueda desde la pantalla C#. |
| RF-M5-03 | Implementado por código y probado en negocio/DAO + MySQL | Emisión, concurrencia, rollback y liberación tienen casos aprobados; QR e id se conservan. | Emisión/cancelación desde C# y definir la política económica pendiente de la JP. |
| RF-M6-02 | Implementado por código | Consulta y pantalla usan entradas `VIGENTE`, incluidas gratuitas, y filtros sede/sala/periodo. | Datos pagados, gratuitos y cancelados mediante C# → REST → MySQL. |
| RF-M6-03 | Implementado por código | Pagos `VALIDADO`, entradas emitidas/canceladas y filtros sede/periodo/tipo presentes. | Importes, conteos, vacío y filtros combinados de extremo a extremo; depende de ventas. |
| RF-M6-04 | Implementado por código | Edición conserva id, fecha y función y persiste descripción, responsable y estado. | Alta, edición y consulta mediante C# → REST → MySQL. |

RF-M6-01 sigue fuera del catálogo propuesto y su implementación histórica no fue eliminada. El catálogo 0.2 permanece pendiente de conformidad de la JP y no fue modificado en este cierre.

### Base de pruebas y migraciones

- **BD utilizada:** esquema aislado `entrefunciones_lab12_test` en el RDS ya configurado, sin registrar host sensible ni credenciales en esta carpeta. El bootstrap aprobado (1/1, 0 fallos) creó/recreó solamente ese esquema e instaló estructura, procedimientos y datos ficticios desde los scripts consolidados.
- La guardia del bootstrap sustituye en memoria solo el `CREATE DATABASE` y `USE entrefunciones` de los scripts canónicos por el esquema autorizado, y rechaza referencias de control a otro esquema. `entrefunciones` y `DB2` no fueron modificados.
- **Migraciones preparadas, no aplicadas incrementalmente:** `20261115_reporte_tipo_evento.sql`, `20261115_incidencias_edicion.sql` y `20261115_disponibilidad_cartelera.sql`. No contienen `USE`, `CREATE/DROP DATABASE`, `entrefunciones` ni `DB2`. Su estado final ya está incluido en los scripts consolidados instalados sobre la BD de prueba, por lo que no se duplicó su ejecución.

### Resultados de verificación y artefactos finales

- **Pruebas unitarias/locales durante el empaquetado:** 47 detectadas; 30 aprobadas, 0 fallos, 0 errores y 17 omitidas por las guardias de BD (16 integradas + 1 bootstrap).
- **Negocio/DAO + MySQL:** bootstrap 1/1; las tres pruebas focales individuales 1/1 cada una; corrida completa 14/16 con dos expectativas obsoletas; corrección focal posterior 2/2. No se repitió la corrida completa.
- **REST HTTP + MySQL:** pendiente; no se inició ni desplegó un servidor REST para esta verificación.
- **C# → REST Java → MySQL:** pendiente; compilar/publicar no se contabiliza como funcionamiento integrado.
- **WAR final:** `Backend/entrefunciones-rest/target/entrefunciones-rest.war`, 14 273 937 bytes, 2026-11-15 20:54:50 -05:00, SHA-256 `1502BDEDA79544D42BEC0A86FDBCB0B3750701AD05515BE6AB7A16B7AACE3F6B`.
- **Frontend IIS/.NET 10:** `Frontend/EntreFunciones/bin/Release/net10.0/publish/`; contiene `web.config`. `EntreFunciones.dll`, 570 880 bytes, 2026-11-15 20:54:41 -05:00, SHA-256 `0B95637A5A07B4E9ADE54DFDB79FCC5F8E3418CB85917AC44D89FB755BA25363`.
- No existen fuentes productivas posteriores a esos artefactos. `git diff --check` no reportó errores de espacios; solo avisos informativos de conversión LF/CRLF.

### Bloqueos y siguiente paso

- No queda un bloqueo de acceso a la BD aislada. Lo pendiente de verificación es de transporte/interfaz: levantar localmente el REST con configuración dirigida exclusivamente a `entrefunciones_lab12_test` y ejecutar los recorridos desde C# sin desplegar.
- La dependencia funcional principal de RF-M6-02 y RF-M6-03 sigue siendo verificar con el mismo conjunto de datos las ventas confirmadas, entradas gratuitas y cancelaciones.
- El único RF pendiente por implementación es RF-M1-03. También siguen pendientes la conformidad de la JP, la política económica de cancelaciones y la definición uniforme de la fecha de periodo de los reportes.

## Cierre LAB12 — pasos 2 a 6 (2026-11-15)

### Resultado de los bloques solicitados

- **PASO 4 — reportes e incidencias:** RF-M6-02 cuenta entradas emitidas `VIGENTE`, incluidas las gratuitas; RF-M6-03 separa pagos `VALIDADO`, entradas emitidas y canceladas y combina sede, periodo y tipo; RF-M6-04 actualiza descripción, responsable y estado sin sustituir el registro histórico.
- **PASO 2 — permisos y publicación:** las mutaciones administrativas tienen control REST de rol y los recursos del cliente validan propietario o administrador. Las consultas públicas y la creación directa de reservas aceptan únicamente funciones `CONFIRMADA`, activas y futuras.
- **PASO 3 — entradas:** una reserva gratuita se confirma y emite sus entradas sin pago; cada entrada conserva identificador y QR independiente. La cancelación individual cambia únicamente esa entrada y libera exactamente su cupo o butaca al excluirla de la disponibilidad.
- **PASO 5 — disponibilidad y cartelera:** se incorporó el CRUD con desactivación lógica de bloqueos de sala; disponibilidad y confirmación validan cruces con bloqueos y funciones confirmadas. La cartelera combina los filtros de fecha, sede, distrito, tipo, temática, precio y accesibilidad y muestra butacas ocupadas y retenidas por separado.
- Se conservaron los estados y datos históricos. La implementación heredada de `UTILIZADA` no fue eliminada, aunque RF-M6-01 ya no pertenezca al catálogo propuesto.

### Verificación final

- **Pruebas unitarias/en proceso:** Maven ejecutó 38 pruebas: **25 aprobadas, 0 fallos, 0 errores y 13 omitidas**. Las aprobadas cubren negocio de auditoría, espacios, funciones, entradas y bloqueos; no representan llamadas HTTP contra MySQL.
- **Backend:** `mvnw.cmd -pl entrefunciones-rest -am package` terminó correctamente y generó el WAR local.
- **Frontend:** `dotnet publish Frontend/EntreFunciones/EntreFunciones.csproj -c Release --no-restore` terminó correctamente y preparó la publicación local.
- **REST + MySQL:** no ejecutado. No se encontró una BD de pruebas identificada ni variables de conexión de prueba. La única conexión conocida corresponde al servidor publicado y no se utilizó.
- **Recorrido C# → REST Java → MySQL:** pendiente por el mismo bloqueo. La compilación y el publish no se contabilizan como funcionamiento integrado.
- **Migraciones preparadas, no aplicadas:** `20261115_reporte_tipo_evento.sql`, `20261115_incidencias_edicion.sql` y `20261115_disponibilidad_cartelera.sql`, en `Backend/sql/migraciones/`.
- **BD utilizada:** ninguna. No se modificó la BD publicada, configuración de AWS ni configuración de despliegue.

### Estado contrastado de los 17 RF

| RF | Estado por código | Evidencia disponible | Pendiente funcional concreto |
|---|---|---|---|
| RF-M1-01 | Implementado | Registro, autenticación y endpoints de datos propios presentes; propiedad reforzada en REST. | Probar alta, login, consulta y edición propia desde C# hasta MySQL. |
| RF-M1-02 | Implementado | Guardas de administrador y propietario añadidas en recursos REST; contenido público filtrado. | Ejecutar pruebas HTTP 401/403 y recorrido por ambos roles contra BD. |
| RF-M1-03 | Pendiente | Existe cambio directo con correo/DNI, pero no un flujo verificado de solicitud y recuperación. | Diseñar e implementar verificación de la solicitud sin exponer credenciales. |
| RF-M2-01 | Implementado por código | CRUD lógico existente; distrito y accesibilidad propagados por modelo, DAO, REST y C#. | Aplicar migración en prueba y verificar conservación histórica desde C#. |
| RF-M2-02 | Implementado por código | CRUD de bloqueos, desactivación lógica y validación al consultar/confirmar; 2 pruebas unitarias aprobadas. | Probar CRUD, solapamientos y confirmación concurrente con REST + MySQL. |
| RF-M2-03 | Parcial | Gestión y mapa de butacas existentes; las entradas conservan la asociación histórica. | Verificar que altas, cambios y desactivaciones no alteren reservas/entradas ya emitidas. |
| RF-M3-01 | Parcial | Datos de contenido y programación existen; temática y público objetivo fueron propagados. | Unificar y probar el flujo completo de registro/edición según cada tipo de actividad. |
| RF-M3-02 | Implementado por código | Consulta previa y revalidación al confirmar contra bloqueos y funciones `CONFIRMADA`; pruebas de negocio aprobadas. | Probar cruces reales y dos confirmaciones concurrentes en MySQL. |
| RF-M3-03 | Implementado por código | Confirmación/cancelación existentes; cartelera y reserva directa exigen `CONFIRMADA` y activos. | Probar transición, publicación y rechazo de reserva directa con REST + MySQL. |
| RF-M4-01 | Implementado por código | Cartelera filtra en conjunto fecha, sede, distrito, tipo, temática, precio y accesibilidad. | Probar todas las combinaciones y detalle público desde C# con datos migrados. |
| RF-M4-02 | Implementado por código | Mapa diferencia libre, ocupada y retenida; reserva mantiene comprobaciones transaccionales. | Probar contención simultánea de la misma butaca y liberación por vencimiento. |
| RF-M5-01 | Implementado por código | Reserva temporal, vencimiento, consulta/cancelación y exclusión por vigencia presentes. | Probar reloj de vencimiento, cancelación y liberación con REST + MySQL. |
| RF-M5-02 | Parcial | Registro/validación de pago y consulta existen; el negocio contiene búsqueda por código. | Confirmar/exponer la búsqueda administrativa por código en REST/C# y probar la validación completa. |
| RF-M5-03 | Implementado por código | Emisión pagada/gratuita, QR único por entrada, historial y cancelación individual implementados; 2 pruebas unitarias aprobadas. | Probar emisión y liberación exacta con MySQL y acordar la política económica. La funcionalidad histórica `UTILIZADA` se conservó por instrucción expresa, fuera del alcance propuesto. |
| RF-M6-02 | Implementado por código | Consulta y pantalla usan entradas `VIGENTE`, aforo, sede/sala/periodo y cero sin resultados. | Validar con datos pagados, gratuitos y cancelados mediante C# → REST → MySQL. |
| RF-M6-03 | Implementado por código | Pagos `VALIDADO`, entradas emitidas/canceladas y filtros combinados sede/periodo/tipo. | Validar importes, conteos y periodo acordado con datos reales; depende de ventas. |
| RF-M6-04 | Implementado por código | Edición de descripción, responsable y estado conserva id, fecha y función; pruebas unitarias aprobadas. | Aplicar procedimiento y probar alta/edición/consulta desde C# hasta MySQL. |

El catálogo continúa pendiente de conformidad de la JP. Hay implementación total por código en 13 RF, parcial en 3 y pendiente en 1; **no se declara el 90 % funcionando**, porque ningún RF fue verificado en este cierre mediante el recorrido completo C# → REST Java → MySQL.

### Artefactos locales preparados

- WAR: `Backend/entrefunciones-rest/target/entrefunciones-rest.war` — 14 273 063 bytes, generado el 2026-11-15 20:05:23 (hora de Lima), SHA-256 `5DB842A437D726B216BC3F26F125C6C901A9FEB33D8AD7D04AA31CD11CFE64F4`.
- Frontend: `Frontend/EntreFunciones/bin/Release/net10.0/publish/`; `EntreFunciones.dll` generado el 2026-11-15 20:05:07.

### Bloqueo y siguiente paso

El bloqueo exacto es la ausencia de una BD MySQL de pruebas identificada y autorizada. El siguiente paso es recibir host, puerto y nombre de esa BD (sin registrar secretos en `CONTEXTO-TA`), aplicar allí las tres migraciones y ejecutar por bloque pruebas REST + MySQL y recorridos C#. La prioridad es ventas/entradas y los dos reportes porque comparten los datos de pago, emisión gratuita y cancelación.

## Avance LAB12 — PASO 5 cerrado por código

- Se incorporó `bloqueo_sala` con CRUD administrativo y desactivación lógica; conserva sala, inicio, fin e historial.
- La consulta horaria comprueba sala/sede activa, bloqueos vigentes y cruces con funciones `CONFIRMADA`. La pantalla de programación la ejecuta antes de guardar; el negocio y los procedimientos vuelven a validarla al confirmar para cubrir peticiones directas.
- Las funciones programadas pueden coexistir como propuestas; la exclusividad se exige al pasar a `CONFIRMADA`, según RF-M3-02.
- Sedes/salas incorporan distrito y accesibilidad; eventos incorporan temática y público objetivo en modelo, REST, persistencia y formularios.
- Cartelera usa únicamente funciones confirmadas, activas y futuras, y combina en la misma función fecha, sede, distrito, tipo, temática, precio y accesibilidad.
- El mapa de butacas distingue `ocupada` (entrada confirmada vigente/utilizada) de `retenida` (reserva temporal activa), además de libre/inactiva/elegida.
- Migración preparada: `Backend/sql/migraciones/20261115_disponibilidad_cartelera.sql`; no aplicada.
- Pruebas unitarias del bloque: 2 aprobadas, 0 fallos, para rango inválido y conservación histórica al desactivar. Suite Java ejecutada: 23 aprobadas y 13 de integración omitidas. Backend REST y frontend C# compilan sin errores.
- **Pendiente integrado:** aplicar la migración en una BD de pruebas y probar CRUD, solapamientos, confirmación concurrente, filtros combinados y estados de butacas mediante C# → REST → MySQL.
- Siguiente paso: verificación final, estado de 17 RF y generación local de WAR/publicación C# sin desplegar.

## Avance LAB12 — PASO 3 cerrado por código

- Las reservas de precio cero se confirman y emiten dentro de la misma transacción, sin crear pago ni ingreso. Cada entrada conserva un id de BD y recibe un QR UUID independiente.
- La cancelación individual bloquea función y entrada, cambia únicamente esa entrada de `VIGENTE` a `CANCELADA` y conserva id, QR, precio, reserva y butaca históricos.
- Disponibilidad cuenta reservas temporales activas y, para reservas confirmadas, solo entradas `VIGENTE`/`UTILIZADA`. Por ello una cancelación libera exactamente su butaca o un cupo general, sin liberar las demás entradas de la reserva.
- “Mis compras” muestra `Vigente`, `Cancelada` o `Utilizada` por entrada y permite cancelar individualmente solo una entrada vigente.
- Pruebas unitarias del bloque: 2 aprobadas, 0 fallos; comprueban identidad/QR, aislamiento entre dos entradas y prohibición de cancelar una utilizada. Backend de negocio y frontend C# compilan.
- **Pendiente integrado:** probar emisión gratuita, unicidad real de QR y liberación exacta con concurrencia mediante REST + MySQL; no se ejecutó por falta de una BD de pruebas identificada.
- No se preparó migración de esquema para este bloque: la solución usa las tablas y estados existentes y modifica consultas transaccionales en Java.
- Siguiente bloque: PASO 5, bloqueos temporales, disponibilidad horaria, salas inactivas y filtros de cartelera.

## Avance LAB12 — PASO 2 cerrado por código

- El filtro de autenticación ahora valida el token cuando un GET público lo incluye; así un administrador conserva su vista completa y una sesión no puede quedar tratada silenciosamente como anónima.
- Las mutaciones REST de eventos y funciones exigen rol administrador. El acceso público a funciones y al detalle/listado de eventos queda limitado a funciones `CONFIRMADA` de contenido activo.
- Los endpoints de clientes/cuentas exigen propietario o administrador; listado/reactivación y administración de administradores exigen administrador.
- Entradas y pagos exigen propietario de la reserva o administrador; listado global y validación/rechazo/validación de ingreso exigen administrador.
- La creación directa de reservas continúa protegida en negocio: solo acepta una función `CONFIRMADA`, activa, futura y con sala/sede activas.
- Verificación: compilación del backend REST y dependencias aprobada. La suite ejecutada registró 21 pruebas aprobadas y 13 pruebas de integración omitidas por no disponer de BD de pruebas. Las decisiones 401/403/404 y la publicación confirmada siguen pendientes de prueba HTTP + MySQL.
- Siguiente bloque: PASO 3, emisión gratuita, estado visible y cancelación individual con liberación exacta.

## Avance LAB12 — 2026-11-15, PASO 4 cerrado por código

- **RF-M6-02:** el reporte de ocupación usa entradas emitidas en estado `VIGENTE` (pagadas o gratuitas), excluye las canceladas y calcula el porcentaje contra el aforo. La pantalla muestra etiquetas de ocupación y conserva filtros combinados de sede, sala y periodo.
- **RF-M6-03:** el reporte separa ingresos de pagos `VALIDADO`, total histórico de entradas emitidas y entradas `CANCELADA`; combina sede, periodo y tipo de actividad. Los filtros no aplicables al reporte elegido no se envían. Sin coincidencias se mantiene la consulta vacía con totales cero.
- **RF-M6-04:** la atención de incidencias permite editar y persistir descripción, responsable y estado sobre el mismo registro. Se conservan id, fecha y función histórica.
- Migraciones preparadas: `Backend/sql/migraciones/20261115_reporte_tipo_evento.sql` y `Backend/sql/migraciones/20261115_incidencias_edicion.sql`.
- Pruebas unitarias del bloque: 10 aprobadas, 0 fallos (`FuncionBLImplTest` y `AuditoriaBLImplTest`). Compilación C# aprobada con 0 errores y 0 advertencias.
- **No probado de forma integrada:** REST + MySQL y recorrido C# → REST → MySQL. No se identificó una BD de pruebas configurada; la única conexión conocida corresponde al servidor publicado y no se utilizó. Ninguna migración fue aplicada.
- **Dependencia de ventas:** comprobar con datos de prueba que pagos validados, emisiones gratuitas y cancelaciones individuales alimenten ambos reportes con el mismo conjunto filtrado.
- **Siguiente paso de implementación:** PASO 2, controles REST de rol/propiedad y publicación exclusiva de funciones confirmadas.

- **Fecha de actualización:** 2026-11-15
- **Catálogo funcional de trabajo:** Versión 0.2 incorporada; los cambios de alcance siguen pendientes de conformidad de la JP.

## Trabajo verificado

- Se confirmó que esta carpeta de contexto está en la raíz real de `proyecto_progra3`, junto a `Backend` y `Frontend`, y no dentro del respaldo LAB11.
- Se prepararon la plantilla del catálogo, este estado y la carpeta `referencias`.
- **Implementación verificada por código:** el reporte propaga el filtro `tipoEvento` desde `Reportes.razor` y `FuncionService` hasta `FuncionesResource`, `FuncionBL`, `FuncionDAO` y el procedimiento `listar_reportes_funciones`. Los valores admitidos son los ya existentes: `PELICULA`, `OBRA_TEATRO` y `ACTIVIDAD_CULTURAL`; “Todos” omite el parámetro.
- El filtro se combina en la consulta con periodo, sede y sala. Los totales de la pantalla se calculan sobre la misma lista devuelta; una lista vacía conserva el estado vacío y muestra totales numéricos en cero, sin recuperar todos los datos.
- Se preparó `Backend/sql/migraciones/20261115_reporte_tipo_evento.sql` para actualizar una BD existente. No se ejecutó la migración.
- No se modificaron el cálculo de asistencia, los estados de entradas ni otros requerimientos funcionales.

## Comprobaciones ejecutadas

- `FuncionBLImplTest`: 4 pruebas unitarias aprobadas para combinación sede + periodo + tipo, opción “Todos”, combinación sin resultados y rechazo de un tipo incompatible.
- Suite unitaria sin BD: 16 pruebas aprobadas, 0 fallos, 0 errores y 0 omitidas (`AuditoriaBLImplTest`, `EspaciosBLImplTest` y `FuncionBLImplTest`).
- Backend: empaquetado de `entrefunciones-rest` y sus dependencias aprobado con Maven; las pruebas se omitieron en ese empaquetado porque ya se ejecutó la selección unitaria por separado.
- Frontend: compilación aprobada con 0 errores y 0 advertencias.
- **Prueba integrada pendiente:** no se conectó a MySQL, no se aplicó la migración y no se ejecutó el recorrido UI → REST → BD con datos reales.

## Pendientes y bloqueos

- Confirmar con la JP los cambios de alcance registrados en la versión 0.2 del catálogo.
- Agregar a `referencias` los lineamientos, observaciones de la JP y transcripciones pertinentes.
- Aplicar la migración del reporte en un entorno controlado y comprobar con datos reales las tres opciones de tipo, sus combinaciones con sede y periodo, y un caso sin coincidencias. Esta acción requiere autorización previa por involucrar la BD.

## Siguiente paso

Con autorización para usar una BD de prueba, aplicar la migración y ejecutar la prueba integrada del reporte sin desplegar a producción.

## Entorno local — 2026-11-16, login con HTTP 500 por JWT

- Síntoma: tras registrarse como cliente, el login mostraba "No se pudo contactar o validar el servicio de autenticación". El `server.log` del GlassFish local registró `Configure ENTREFUNCIONES_JWT_SECRET con al menos 32 bytes` en `JwtServicio.cargarClave`; las credenciales eran válidas.
- Cambio: se registró `-DENTREFUNCIONES_JWT_SECRET` como opción de JVM en el dominio GlassFish local (`asadmin create-jvm-options`), con un valor aleatorio que no se guarda en el repositorio. No se modificó código ni `db.properties`.
- Pendiente: reiniciar GlassFish para que tome la opción y probar el login. No probado todavía.
- Revertir: `asadmin delete-jvm-options` sobre esa opción y reiniciar el dominio.
- Además se agregó `jwt.secret` (valor aleatorio de 64 caracteres) a `ConfiguracionCompartida/db.properties`, que `JwtServicio` lee cuando no hay propiedad de JVM ni variable de entorno. Queda versionado junto con las credenciales de BD por decisión del usuario; sin commit todavía. Requiere recompilar y redesplegar el WAR.
- El servidor publicado sigue con el error hasta que reciba un WAR recompilado con esta línea o su propia clave externa.

## Entorno local — 2026-11-16, cartelera con HTTP 500

- Síntoma: tras reiniciar GlassFish (16:27), `GET /funciones` y `GET /eventos` devuelven 500 ("No se pudo listar las funciones" / "No se pudo listar los eventos"); el detalle de la `SQLException` no se registra en `server.log`.
- Causa probable, no confirmada en BD: el commit `76d94e5` (PR #27) hizo que el backend lea `sede.distrito`, `sala.accesible`, `evento.tematica` y `evento.publico_objetivo`, creadas por `20261115_disponibilidad_cartelera.sql`, migración que según este mismo documento no se aplicó al esquema `entrefunciones`. No se consultó la BD.
- Cambio local: se registró `-DDB_NAME=entrefunciones_lab12_test` como opción de JVM del GlassFish local para que el REST local use el esquema de pruebas ya instalado con los scripts consolidados. No se modificó ninguna BD, código ni `db.properties`.
- Pendiente: reiniciar GlassFish y probar cartelera y login. No probado. Las cuentas creadas en `entrefunciones` no existen en el esquema de pruebas.
- Revertir: `asadmin delete-jvm-options -DDB_NAME=entrefunciones_lab12_test` y reiniciar.
- **Corrección 16:35:** las opciones registradas con `asadmin` (`ENTREFUNCIONES_JWT_SECRET` y `DB_NAME`) no sobreviven: IntelliJ regenera `domain.xml` en cada arranque a partir de "VM options" de la configuración "GlassFish 8.0.2" (`domain.jvm-options-IDEA.xml`, `domain.xml.bak`). Tras el reinicio de las 16:32 ninguna de las dos está activa. El JWT sí funciona por `jwt.secret` en el `db.properties` empaquetado (verificado en el JAR desplegado). `DB_NAME` debe añadirse en VM options de IntelliJ; pendiente del usuario.
- **Decisión del usuario 16:40:** no usar esquema de pruebas; el proyecto debe funcionar desde cualquier máquina contra `entrefunciones`. Se comprobó que no queda ninguna opción `DB_NAME` en GlassFish ni en IntelliJ.
- Se preparó `Backend/sql/migraciones/20261116_actualizar_bd_compartida.sql` (idempotente, MySQL 8 y MariaDB): agrega `sede.distrito`, `sala.accesible`, `evento.tematica`, `evento.publico_objetivo` y la tabla `bloqueo_sala` solo si faltan. Después debe ejecutarse `Backend/sql/procedimientos.sql` completo. **No ejecutado**: requiere autorización por modificar la BD compartida. Sintaxis no probada contra un servidor.
- **17:17, causa confirmada:** `SQLSyntaxErrorException: Column 'tematica' not found` al listar funciones y eventos. Se agregó registro SEVERE de la causa en `FuncionesResource.error` y `EventosResource.error` para respuestas 5xx (antes se perdía).
- Los scripts se ejecutaron a las 17:08 sin errores, pero en la conexión de Workbench "AGENTEC" (otro servidor RDS), no en el servidor de `ConfiguracionCompartida/db.properties` que usa el backend. Falta ejecutarlos en el servidor correcto.
- **17:38, resuelto:** el usuario ejecutó ambos scripts en el servidor de `db.properties` (conexión Workbench "TRABAJO_FINAL"). `GET /funciones` y `GET /eventos` responden 200 con datos. El servidor de la conexión "AGENTEC" también recibió los scripts a las 17:08.

## Frontend — 2026-11-16, validación del formulario de tarjeta

- `Components/Pages/Cliente/ReservaPago.razor`: el pago (simulado) ahora valida en pantalla titular (nombre y apellido, solo letras, en mayúsculas), número (16 dígitos agrupados de 4 en 4, solo Visa/Mastercard, dígito verificador Luhn), vencimiento (MM/AA con barra automática, mes 01-12, no vencida, máximo 15 años) y CVV (3 dígitos). Los errores aparecen al salir del campo o al intentar pagar; no se abre la confirmación si hay errores.
- Compilación aprobada (0 errores, 0 advertencias). No probado en el navegador: llegar al paso de pago requiere crear una reserva en la BD compartida.
- Sin resolver para el equipo: aplicar las migraciones del 15/11 a `entrefunciones`. La migración usa `ADD COLUMN IF NOT EXISTS`, sintaxis de MariaDB que MySQL 8 no acepta; verificar el motor antes de ejecutarla.
