# Estado de EntreFunciones

## Integración Git de la entrega LAB12 (2026-11-17)

### Integración y contenido preparado

- La rama de trabajo `update` incorporó `origin/main` mediante una fusión normal, sin rebase ni reescritura de historia. Se conservaron los cambios del equipo sobre login, cartelera, compra, QR y validación de tarjeta junto con las fechas opcionales de reportes. El único conflicto fue documental en este archivo y se resolvió conservando ambos historiales.
- La entrega versionada incluye el código productivo ya presente en `update`, las migraciones de reportes e incidencias y el nuevo `Backend/sql/migraciones/ROLLBACK_20261116_reportes_fechas_opcionales.sql`. No se incluyeron fixtures ni datos de prueba.
- `docs/GUIA-ACCESO.md` distingue el frontend público `18.206.246.134`, el backend público `98.81.195.228`, el backend privado `172.31.21.178`, la configuración interna de AWS mediante `Rest__BaseUrl` y el valor local `localhost`. No se cambió `DB_NAME`.
- `docs/reportes-incidencias.md` documenta RF-M6-02, RF-M6-03 y RF-M6-04, el orden de migración para otro esquema y la reversión de fechas opcionales. `.gitignore` excluye `.verification/` y `Entrega-AWS*/`, además de los patrones existentes para secretos y artefactos generados.
- Los borrados locales del respaldo histórico LAB11 se mantuvieron fuera de la integración y del commit para no mezclar ni eliminar trabajo ajeno.

### Comprobaciones y bloqueo

- Por el conflicto documental se ejecutó `mvnw.cmd -pl entrefunciones-rest -am package -DskipTests`: **BUILD SUCCESS** para los seis módulos. No se ejecutaron pruebas ni se accedió a la BD.
- `dotnet build Frontend/EntreFunciones/EntreFunciones.csproj -c Release --no-restore` se detuvo porque `QRCoder`, agregado previamente en `main`, no está en la caché local. No se ejecutó `dotnet restore` para evitar descargar dependencias sin autorización. Este bloqueo no proviene de la resolución del conflicto.
- No se repitieron las 8 pruebas integradas, no se modificó AWS/RDS y no se copiaron registros sensibles. La migración de fechas opcionales permanece confirmada en el RDS y esquema `entrefunciones_lab12_test` usados por el backend remoto.
- Siguiente paso: publicar `update` y abrir la PR hacia `main`; la revisión deberá ejecutar la restauración autorizada de dependencias y la compilación .NET.

## PASO 6.2 — migración focal y recorrido integrado de fechas (2026-11-17, 07:12 hora de Lima)

### Migración aplicada y objetos conservados

- Con autorización expresa se aplicó exactamente `Entrega-AWS-Lab12-Fechas-Vacias-20261116/BaseDatos/20261116_reportes_fechas_opcionales.sql`, SHA-256 `75DD83E8CD845BE1EF820377ADEE5E52AE96EE67E6ADF9CE6B37C54F55BEF34A`, exclusivamente sobre `entrefunciones_lab12_test`.
- El único objeto SQL afectado es el procedimiento `listar_reportes_funciones`. El script no contiene `USE`, operaciones sobre bases de datos, nombres de otro esquema ni DML; la utilidad protegida comprobó `SELECT DATABASE()` antes de ejecutar, después de ejecutar y durante la inspección final.
- La definición previa ya admitía fechas opcionales, por lo que la migración fue idempotente. El `SHOW CREATE PROCEDURE` previo y posterior conservó el mismo SHA-256 `2910CDAB835922F03635B80C8151335E81C983090128F2D3192B8D515A8B45E0`. La captura exacta previa está en `.verification/step-6.2-20261117/PRECAMBIO-listar_reportes_funciones.txt`.
- Los conteos de las 17 tablas se conservaron individualmente: 254 filas antes y 254 después de la migración y del recorrido focal. No se ejecutó DML ni se recreó el esquema. La captura previa está en `.verification/step-6.2-20261117/PRECAMBIO-conteos.txt`.

### Recorrido focal C# → REST Java → MySQL

- Se redesplegó temporalmente en el GlassFish local aislado el WAR nuevo del paquete, SHA-256 `345C51327369F77180A8C5BD18E313F3BCF64AFD52AC050188A813C66861609B`, con `DB_NAME=entrefunciones_lab12_test`.
- El verificador C# cargó `EntreFunciones.dll` con SHA-256 `0E345194BF34A577876E8D5F8B708174C2AB8254891C92CCF556CA56DDF6EE2D`, idéntico al DLL incluido en la publicación nueva del paquete.
- Modo ejecutado: exclusivamente `m6-optional-dates`; resultado **8/8 PASS**, 0 fallos. Aprobó autenticación requerida, ambos límites vacíos, solo `desde`, solo `hasta`, rango inclusivo, rango invertido rechazado por REST con HTTP 400, rango invertido rechazado por C# antes de consultar y rango sin resultados con lista/totales en cero. Fixture: `STEP6-20261115231300-9942`.
- No se ejecutaron suites Maven, suites generales, pruebas de incidencias ni aceptación de navegador. El GlassFish local quedó detenido al terminar.
- `entrefunciones`, DB2 y AWS no fueron conectados, modificados ni desplegados durante este paso.

### Entrega manual pendiente en AWS

1. Respaldar el procedimiento vigente, el WAR desplegado y la publicación frontend vigente.
2. Aplicar `BaseDatos/20261116_reportes_fechas_opcionales.sql` en el esquema AWS confirmado.
3. Desplegar `Backend/entrefunciones-rest.war` en GlassFish.
4. Reemplazar la publicación completa del sitio por todo el contenido de `Frontend/`; no copiar solamente `EntreFunciones.dll`.
5. Ejecutar el humo focal de fechas. Para revertir los binarios, restaurar frontend y WAR anteriores; para volver a fechas obligatorias, usar `BaseDatos/ROLLBACK_20261116_reportes_fechas_opcionales.sql` sobre el mismo esquema autorizado.

## Ajuste de fechas vacías en reportes y paquete final (2026-11-16, 22:25 hora de Lima)

### Implementación por código

- **Terminado y empaquetado.** RF-M6-02 y RF-M6-03 aceptan ambos límites de fecha vacíos, únicamente `desde`, únicamente `hasta` o el rango completo. Si ambas fechas existen, se conserva el rechazo de un rango invertido.
- El frontend usa `DateOnly?`, omite de la URL los límites vacíos, muestra un resumen acorde al rango y agrega `Limpiar`, que restablece todos los filtros y consulta todo el periodo.
- REST interpreta cada fecha como parámetro opcional; negocio valida el orden solo cuando ambas existen; DAO envía `NULL` SQL para el límite ausente. El procedimiento `listar_reportes_funciones` aplica cada límite de forma independiente e inclusiva.
- Se conserva como fecha del periodo la fecha de la función y no se modificaron los cálculos de ocupación, ingresos, entradas emitidas o canceladas. La definición uniforme de la fecha de periodo continúa pendiente de conformidad de la JP según el catálogo.

### Verificación ejecutada

- Prueba focal `FuncionBLImplTest`: **7/7 aprobadas**, 0 fallos y 0 omitidas. Incluye ninguna fecha, solo desde, solo hasta y rango invertido.
- Empaquetado Maven final: **BUILD SUCCESS**. En negocio se detectaron 50 pruebas: 32 aprobadas, 0 fallos, 0 errores y 18 omitidas por sus guardias de BD. En REST se detectaron 3 pruebas de integración: 3 omitidas por su guardia de entorno. El WAR fue generado correctamente.
- `dotnet publish -c Release --no-restore`: aprobado. Se creó una publicación nueva directamente dentro del paquete final.
- Integridad del paquete: 49 archivos y 17,166,361 bytes. WAR, DLL y migración copiados coinciden con sus orígenes por SHA-256.
- **No probado de forma integrada:** no se aplicó la migración, no se modificó ninguna BD y no se desplegó frontend ni WAR. Falta comprobar el recorrido C# → REST Java → MySQL con los cuatro casos de fechas en un esquema autorizado.

### Paquete y reemplazo requerido

- Carpeta final: `Entrega-AWS-Lab12-Fechas-Vacias-20261116/`.
- Frontend completo: `Frontend/`, con `EntreFunciones.dll` SHA-256 `0E345194BF34A577876E8D5F8B708174C2AB8254891C92CCF556CA56DDF6EE2D`.
- WAR: `Backend/entrefunciones-rest.war`, SHA-256 `345C51327369F77180A8C5BD18E313F3BCF64AFD52AC050188A813C66861609B`.
- Migración: `BaseDatos/20261116_reportes_fechas_opcionales.sql`, SHA-256 `75DD83E8CD845BE1EF820377ADEE5E52AE96EE67E6ADF9CE6B37C54F55BEF34A`. Se incluye también un rollback del procedimiento.
- **Requiere actualizar frontend completo, WAR y procedimiento de BD; no basta reemplazar solo el frontend.** `LEEME-DESPLIEGUE.md` indica el orden y la reversión. El paquete previo `Entrega-AWS-Lab12/` no fue sobrescrito.

### Siguiente paso y autorización pendiente

- Con autorización explícita sobre el esquema de destino: respaldar el procedimiento vigente, aplicar la migración, desplegar WAR y frontend y hacer el humo de fechas vacías/único límite/rango completo/rango invertido.
- Para revertir: restaurar el frontend y WAR anteriores y, si se desea volver a fechas obligatorias, ejecutar el rollback incluido sobre el mismo esquema autorizado.

## Aceptación mínima real en navegador de RF-M6-02, RF-M6-03 y RF-M6-04 (2026-11-16, 21:32 hora de Lima)

### Estado y entorno ejecutado

- **Terminado, no bloqueado.** No había una ejecución previa de Playwright ni Playwright instalado en el proyecto. Se realizó la aceptación solicitada en **Chrome visible 142.0.7444.176**, controlado mediante Chrome DevTools Protocol y reutilizando los verificadores y datos focales existentes; no se instaló ninguna dependencia.
- Frontend del paquete vigente servido temporalmente en `http://127.0.0.1:16100`; REST local en `http://127.0.0.1:16080/entrefunciones-rest/v1/api/`; dominio GlassFish aislado `.verification/glassfish-domains/lab12-login-test`; esquema efectivo exigido y comprobado: **`entrefunciones_lab12_test`**. El fixture identificable fue `STEP6-20261115231300-9942`.
- El registro de red no contiene solicitudes a EC2 ni AWS. La aplicación cargó sus dependencias visuales ya configuradas desde `cdn.jsdelivr.net` (Bootstrap, Bootstrap Icons y Bootstrap JS). Al finalizar se detuvieron el frontend y GlassFish locales; solo quedó abierto el informe final local en Chrome.
- Antes del navegador se ejecutó únicamente el verificador focal existente `.verification/Lab12Step6/bin/Release/net10.0/Lab12Step6.dll` con `EF_STEP6_MODE=m6-focused`: **11/11 PASS**, con `incident_update=not-executed`. No se ejecutó ninguna suite general.

### Resultados ejecutados en navegador

- **RF-M6-02 — PASS, 13/13 comprobaciones.** Con sede, sala y periodo `30/12/2026–31/12/2026`: función pagada `1/4 = 25.00 %`, función adyacente `0/4 = 0.00 %`, función gratuita `2/4 = 50.00 %`; totales `3 funciones` y `3 entradas`. Para `01/01/2035` se mostró el resultado vacío y totales `0/0`.
- **RF-M6-03 — PASS, 16/16 comprobaciones.** Con sede del fixture, periodo `30/12/2026–31/12/2026` y tipo `PELÍCULA`: `S/ 75.00`, `2 entradas` emitidas y `1 entrada` cancelada; la fila adyacente quedó en `S/ 0.00`, `0/0`, y la actividad gratuita no generó ingreso. Con `OBRA_TEATRO` se obtuvo vacío `S/ 0.00`, `0/0`. Las tres tarjetas conservaron `text-nowrap` y `white-space: nowrap` calculado.
- **RF-M6-04 — PASS, 13/13 comprobaciones.** Se buscó y reabrió la incidencia persistida **#3** del fixture. Lista y detalle conservaron función, fecha `16/11/2026, 04:31`, descripción `STEP6-20261115231300-9942 descripcion actualizada focal M6`, responsable `Katita Laboratorio` y estado `En proceso`. Esta comprobación fue de solo lectura: no se pulsó «Guardar cambios».
- Resultado final conjunto: **3/3 casos PASS, 42 comprobaciones, 0 fallos**. Inicio `16/11/2026 21:14:03 PET`; fin `16/11/2026 21:32:13 PET`.
- Los primeros recorridos detectaron esperas insuficientes del verificador durante la transición SSR→interactiva de Blazor, no fallos funcionales del producto. Se corrigieron únicamente las esperas del arnés y se repitieron solo los casos afectados: primero RF-M6-02/RF-M6-03 y después RF-M6-02. RF-M6-04 no se repitió después de aprobar. Los informes de esos intentos se conservaron separados.

### Evidencia, prueba manual y paquete

- Informe final abierto: `.verification/browser-acceptance-m6/informe.html`; resultado estructurado: `.verification/browser-acceptance-m6/resultados.json`; seis capturas finales: `.verification/browser-acceptance-m6/capturas/01-rf-m6-02-ocupacion-con-datos.png` a `06-rf-m6-04-incidencia-detalle.png`. El arnés reproducible quedó en `.verification/browser-acceptance-m6/acceptance.js`.
- **Prueba manual de la usuaria, conservada por separado:** alta de una incidencia y edición posterior desde el frontend desplegado, ambas persistentes. Es evidencia comunicada por la usuaria; no se ejecutó ni se atribuye a la automatización de este cierre. La prueba automática actual solo reabrió y leyó una incidencia identificable.
- No hubo cambios de código de producto ni defectos funcionales que corregir; solo se añadieron el arnés/evidencias de verificación y este registro. Por tanto, **no se reconstruyó ni cambió el paquete**. Continúan vigentes `Entrega-AWS-Lab12/Frontend/` (DLL SHA-256 `EC4FCF16CAC4A55E3F939299C3B372B8E470A8C60AAD0C1F782739D672E65C13`) y el WAR `Backend/entrefunciones-rest/target/entrefunciones-rest.war` (SHA-256 `0041FB8525620B7BEBC9B89C2E911565BD414AC5086BD9B413CE2098952EE34C`). El manifiesto vigente es `Entrega-AWS-Lab12/MANIFIESTO-SHA256.md` (SHA-256 `0B350E5FAE54658A204DF85B97BA2B1190CAA692398140598C01B5BC45896C9B`).
- **Pendiente concreto fuera de esta aceptación local:** desplegar el frontend completo de `Entrega-AWS-Lab12/Frontend/` en AWS y hacer el humo mínimo de los tres recorridos contra el esquema de prueba; el WAR no necesita reemplazo para los cambios frontend ya empaquetados. No hay operación de prueba activa ni pendiente local.

## Corrección del listado administrativo de funciones (2026-11-16, 12:14 hora de Lima)

### Implementación y conservación de alcances

- Se separó la lectura pública `ICatalogoService.ListarFunciones()` de la nueva lectura `ListarFuncionesAdministrativas()`. La segunda obtiene el token de la sesión mediante `HttpUtils.CrearCabecerasAutenticadas` y envía `Authorization: Bearer` al mismo `GET /funciones`.
- `/admin/funciones` y la carga de una función para edición usan la consulta administrativa. Cartelera conserva la llamada pública sin token.
- REST no necesitó cambios: `AccesoRest` permite el GET público sin cabecera, pero valida cualquier token recibido y construye el rol real desde la cuenta activa; `FuncionesResource.findAll()` devuelve `FuncionBL.findAll()` completo para `Administrador` y solo `CONFIRMADA` con evento, sala y sede activos para los demás casos. DAO/procedimiento `listar_funciones` no elimina estados ni registros históricos.
- La creación de reservas conserva su guardia de negocio: exige función `CONFIRMADA`, evento/sala/sede activos y fecha futura. No se modificaron reservas, reportes, sus procedimientos ni `SummeryCard`; permanecen los conteos `N0`, el importe `N2` y `text-nowrap`.

### Verificación de solo lectura contra `entrefunciones_lab12_test`

- **Pública, REST AWS:** `GET /funciones` sin token terminó HTTP 200 y Postman registró 3/3 aserciones aprobadas, salida 0. Devolvió exactamente `[1, 2, 15]`, las tres `CONFIRMADA` con evento/sala/sede activos.
- **Administrativa, REST AWS:** se leyó por `SELECT` un administrador activo del esquema de prueba; correo y contraseña se mantuvieron en memoria y no se imprimieron. Login terminó HTTP 200 y `GET /funciones` con el token terminó HTTP 200; 4/4 comprobaciones aprobaron. Devolvió exactamente `[1, 2, 15, 16, 17, 18, 19]`.
- Distribución administrativa observada: 6 `CONFIRMADA`, 0 `PROGRAMADA` y 1 `CANCELADA`. El esquema no contiene actualmente una programada con la cual observar ese estado por HTTP; la ausencia no proviene de un filtro, porque la rama administrativa devolvió los siete registros de `findAll()` y no aplica condición de estado o activo.
- Exclusiones públicas explicadas: 16, 17 y 19 son confirmadas, pero su sede está inactiva; 18 está cancelada y además su sede está inactiva. La consulta administrativa incluyó las cuatro por revisión histórica. No hubo IDs ausentes en la consulta administrativa.
- Las conexiones JDBC usadas para contrastar el fixture se marcaron como solo lectura y finalizaron con `rollback`. No hubo DML, DDL, scripts de carga, cambios de `DB_NAME`, borrado de STEP6/cuentas ni despliegue en AWS.

### Evidencia manual comunicada y artefactos

- **RF-M6-04 — prueba manual reportada por la usuaria:** la usuaria comprobó desde el frontend desplegado el alta de una incidencia y su edición posterior, ambas persistentes. Se registra como evidencia manual de navegador reportada por la usuaria; no fue repetida ni observada de forma independiente en este cierre y no acredita por sí sola cobertura exhaustiva del RF.
- `dotnet publish -c Release --no-restore` terminó correctamente. **Frontend vigente:** `Frontend/EntreFunciones/bin/Release/net10.0/publish/EntreFunciones.dll`, 575 488 bytes, 2026-11-16 12:12:53 -05:00, SHA-256 `EC4FCF16CAC4A55E3F939299C3B372B8E470A8C60AAD0C1F782739D672E65C13`.
- **WAR sin cambios:** `Backend/entrefunciones-rest/target/entrefunciones-rest.war`, SHA-256 `0041FB8525620B7BEBC9B89C2E911565BD414AC5086BD9B413CE2098952EE34C`. No necesita reemplazo para esta corrección.
- `Entrega-AWS-Lab12/Frontend` se actualizó desde la publicación vigente. El paquete conserva 45 archivos (WAR + 44 frontend), 17 154 514 bytes, con 45/45 coincidencias por tamaño y SHA-256 y 45 filas de manifiesto sin discrepancias. SHA-256 de `MANIFIESTO-SHA256.md`: `0B350E5FAE54658A204DF85B97BA2B1190CAA692398140598C01B5BC45896C9B`.
- Para aplicar la corrección se debe reemplazar el contenido completo del frontend por `Entrega-AWS-Lab12/Frontend/`. No se debe reemplazar el WAR por esta tarea. La publicación y la entrega están preparadas localmente; no se desplegaron ni se verificó todavía `/admin/funciones` en navegador con la nueva DLL.

## Diagnóstico Funciones/Reportes y publicación visual (2026-11-16, 12:00 hora de Lima)

### Evidencia de datos y esquema desplegado

- Se ejecutaron exclusivamente consultas `SELECT`, dentro de una conexión marcada como solo lectura y cerrada con `rollback`, sobre `entrefunciones_lab12_test` y `entrefunciones`. No se ejecutaron cargas, DDL, DML, suites ni pruebas; no se cambió `DB_NAME`, no se borraron datos STEP6 ni cuentas y no se alteró GlassFish.
- `entrefunciones_lab12_test` contiene 7 funciones: 6 `CONFIRMADA` y 1 `CANCELADA`. Tres confirmadas tienen evento, sala y sede activos; otras 3 confirmadas y la cancelada tienen evento/sala activos pero sede inactiva. Por ello, exactamente 3 cumplen simultáneamente `CONFIRMADA` + evento activo + sala activa + sede activa.
- `entrefunciones` contiene 6 funciones: 3 `CONFIRMADA`, 2 `PROGRAMADA` y 1 `CANCELADA`; las seis tienen evento, sala y sede activos. Por ello también tiene 3 funciones públicamente visibles, pero no contiene registros STEP6.
- El REST AWS de solo lectura `GET /funciones` devolvió 3 filas: IDs 1, 2 y 15, todas `CONFIRMADA`, fechadas 2026-11-18 y con los mismos títulos que `entrefunciones_lab12_test`. Esa firma no coincide con `entrefunciones`, cuyas funciones tienen otros IDs/fechas y ningún título STEP6. Por comportamiento y datos, el despliegue consulta `entrefunciones_lab12_test`.
- No se leyó la variable efectiva `DB_NAME` del proceso remoto ni su configuración de servidor. La confirmación remota anterior es por una firma de datos inequívoca del endpoint desplegado. En contraste, los `db.properties` locales versionados/configurados conservan `esquema=entrefunciones` y no había una variable local `DB_NAME`; ese valor es solo el predeterminado local, no evidencia del entorno AWS.

| Esquema | Total | Confirmadas | Programadas | Canceladas | Relaciones evento/sala/sede activas | Visibles en consulta pública |
|---|---:|---:|---:|---:|---:|---:|
| `entrefunciones_lab12_test` | 7 | 6 | 0 | 1 | 3 | 3 |
| `entrefunciones` | 6 | 3 | 2 | 1 | 6 | 3 |

### Causa confirmada de 3 filas en Funciones y 6 en Reportes

- La pantalla administrativa **Funciones** llama `CatalogoService.ListarFunciones()`. Ese método realiza `GET /funciones` sin cabecera autenticada incluso si existe una sesión de administrador. `FuncionesResource.findAll()` obtiene primero todas las filas mediante negocio/DAO (`listar_funciones` no filtra) y, al recibir la llamada como pública, conserva únicamente `CONFIRMADA` con evento, sala y sede activos. En el esquema desplegado el resultado exacto son los IDs 1, 2 y 15.
- **Reportes** usa `FuncionService.FindReportes()`, que sí crea cabeceras autenticadas, y REST exige rol `Administrador`. `listar_reportes_funciones` filtra solo por intervalo de fecha y por sede, sala o tipo cuando esos parámetros se informan; no contiene condición por `f.estado`, `e.activo`, `s.activo` ni `se.activo`.
- En `entrefunciones_lab12_test` existen, además de las tres funciones visibles, tres confirmadas STEP6 con sede inactiva dentro del bloque de fechas de pruebas: dos `PAGO` y una `GRATIS`. El reporte puede devolver las seis porque no aplica el filtro público de activos. La cancelada `BORRADOR` queda en 2027-01-01 y depende del intervalo seleccionado. No hay evidencia de datos borrados y no se atribuye la diferencia a una eliminación.

### Ajuste visual, publicación local y entrega

- `Frontend/EntreFunciones/Components/Shared/SummeryCard.razor` cambió solo la representación: conteos con formato `N0`, importe con `N2`, orden legible valor/unidad y clase `text-nowrap` para mantenerlos en una sola línea. Las propiedades `TotalIngresos`, `TotalEntradasVigentes`, `TotalEntradasEmitidas`, `TotalEntradasCanceladas` y `resultados.Count` no cambiaron.
- Se ejecutó únicamente `dotnet publish -c Release --no-restore`, requerido por el cambio Razor; terminó correctamente. No se ejecutó ninguna prueba. La publicación sigue incluyendo la URL REST `http://ec2-34-229-12-45.compute-1.amazonaws.com:8080/entrefunciones-rest/v1/api/`.
- **Frontend publicado localmente:** `Frontend/EntreFunciones/bin/Release/net10.0/publish/EntreFunciones.dll`, 574 464 bytes, 2026-11-16 11:54:32 -05:00, SHA-256 `A174041759A8686009EF2FA0929C7913C92E61A00E81D0A912D3F72B3D04D1B8`.
- **WAR sin cambios:** `Backend/entrefunciones-rest/target/entrefunciones-rest.war`, 14 275 142 bytes, 2026-11-15 23:25:03 -05:00, SHA-256 `0041FB8525620B7BEBC9B89C2E911565BD414AC5086BD9B413CE2098952EE34C`.
- `Entrega-AWS-Lab12/Frontend` se actualizó desde la publicación vigente, sin usar `Entrega-AWS`. Los 45 archivos del paquete (WAR + 44 del frontend) coinciden 45/45 con sus originales en tamaño y SHA-256; el manifiesto contiene 45 filas sin discrepancias. SHA-256 del manifiesto: `E9B5592DC7C13CCAF92CF8C5A49E4CF69F061020A86F12E7A01CC77C96A28EA1`.
- La corrección está preparada localmente, pero el nuevo frontend no se desplegó ni se verificó en navegador/AWS. Tras desplegar quedan por comprobar las cinco tarjetas de ambos reportes: enteros sin decimales, ingresos con dos decimales, ausencia de salto entre valor y unidad, totales sin cambios y comportamiento responsivo. RF-M1-03 permanece pendiente.

## Cierre integrado y paquete local LAB12 (2026-11-16)

### Alcance y estado de la evidencia

- Este bloque actualiza el corte posterior al diagnóstico de login y conserva íntegramente el historial que aparece debajo. La discrepancia anterior era de lectura: el archivo está codificado en UTF-8 sin BOM y el intento fallido lo interpretó como ANSI; no se perdió contenido.
- El catálogo funcional vigente continúa siendo la versión 0.2, con 17 RF y conformidad de la JP pendiente. RF-M1-03 se mantiene pendiente; RF-M6-01 sigue fuera del catálogo propuesto y no se eliminó su implementación histórica.
- No se repitieron las 29 comprobaciones focales finales ni `IntegracionLab11Test`, porque esa suite limpia tablas completas y contradiría la conservación de datos solicitada.
- En el corte final no había pruebas, Maven, .NET ni GlassFish locales en ejecución. No se modificó AWS ni una BD publicada.

### Resultados finales disponibles

- **Empaquetado local más reciente:** los reportes Surefire conservados a las 23:24-23:25 registran 50 pruebas detectadas, 30 aprobadas, 0 fallos, 0 errores y 20 omitidas por guardias: 16 integradas, 1 bootstrap, 2 de login REST y 1 de aprovisionamiento. Esta ejecución valida el empaquetado y las pruebas locales; no sustituye la evidencia integrada.
- **Suite integrada completa anterior:** `IntegracionLab11Test` ejecutó 16 casos contra `entrefunciones_lab12_test`: 14 aprobados, 2 fallidos y 0 omitidos. Los dos fallos tenían una precondición obsoleta con funciones `PROGRAMADA`; tras usar `CONFIRMADA`, ambos aprobaron focalmente 2/2. No existe una corrida consolidada posterior de 16/16.
- **Focales negocio/DAO + MySQL:** bootstrap 1/1; RF-M2-03, RF-M3-01 y RF-M5-02, 1/1 cada uno; login/JWT 2/2; aprovisionamiento de administrador 1/1.
- **Verificador aditivo final:** una ejecución alcanzó 32 comprobaciones aprobadas y se detuvo por timeout al listar entradas. El mismo `GET /entradas?idReserva=31` tardó 29.852 s, por encima del timeout C# de 20 s.
- **Corrección del timeout:** `EntradaDAOImpl.findByReserva` y `EntradasResource` reutilizan la reserva y sus butacas ya cargadas, evitando recargar el grafo por cada entrada. No cambió el esquema ni el contrato DTO. La misma lectura aprobó por HTTP en 11.372 s y mediante `CompraService` C# en 9.319 s, conservando dos entradas vigentes y dos QR únicos.
- **Reanudación focal:** 29/29 comprobaciones pendientes aprobaron sobre el fixture existente, sin repetir los 32 casos previos. Cubrieron cancelación individual y liberación exacta, emisión gratuita y QR, publicación/permisos, bloqueos y solapamientos, cartelera, reportes e incidencias.
- **Navegador:** no ejecutado para este cierre. La evidencia C# corresponde a servicios reales y al login con formulario/antiforgery ya comprobado; no equivale a verificar todas las pantallas visualmente.
- **AWS:** desconocido. No se comprobó qué WAR, publicación frontend, esquema o configuración efectiva están desplegados actualmente.

### Matriz final de los 17 RF

`Sí` indica al menos un escenario representativo aprobado en esa capa, no cobertura exhaustiva de todo el RF. `Desconocido` indica ausencia de evidencia disponible.

| RF | Código | Negocio/DAO | HTTP REST → MySQL | Servicios C# → REST → MySQL | Navegador | AWS |
|---|---|---|---|---|---|---|
| RF-M1-01 | Implementado | Parcial: autenticación/JWT | Parcial: alta, login y perfil propio | Parcial: login, redirección y cookie | Pendiente: edición propia y cierre de sesión | Desconocido |
| RF-M1-02 | Implementado | Desconocido como prueba aislada | Sí: autenticación, rol, propiedad y rechazos 401/403 | Parcial: sesión administrativa; las restricciones se comprobaron principalmente por HTTP directo | Pendiente con ambos roles | Desconocido |
| RF-M1-03 | **Pendiente** | Desconocido | Desconocido | Desconocido | Pendiente | Desconocido |
| RF-M2-01 | Implementado | Parcial: pruebas transaccionales locales; recorrido MySQL aislado no consolidado | Sí: alta, edición y baja lógica | Sí mediante servicios de catálogo | Pendiente: CRUD visual e historial | Desconocido |
| RF-M2-02 | Implementado | Parcial: 2 unitarias; prueba DAO/MySQL separada no consolidada | Sí: bloqueo, disponibilidad, solapamiento y desactivación | Sí mediante servicio de bloqueos | Pendiente en pantalla administrativa | Desconocido |
| RF-M2-03 | Implementado | Sí: focal MySQL 1/1 | Sí: edición, desactivación e historial | Sí | Pendiente: mapa e historial visual | Desconocido |
| RF-M3-01 | Implementado | Sí: focal MySQL 1/1 | Sí | Sí: contenido y programación inicial | Pendiente para los tres tipos y edición | Desconocido |
| RF-M3-02 | Implementado | Sí: focal MySQL 2/2 | Sí: bloqueos, cruces y límite adyacente | Sí | Pendiente: validaciones y mensajes de formulario | Desconocido |
| RF-M3-03 | Implementado | Sí: cancelación persistente | Sí: programada no pública, confirmada pública y cancelada histórica | Sí | Pendiente: publicación y retiro visibles | Desconocido |
| RF-M4-01 | Implementado | Desconocido como prueba DAO/MySQL independiente | Sí: filtros combinados y conjunto vacío | Sí mediante `FuncionService` | Pendiente: combinaciones y detalle visual | Desconocido |
| RF-M4-02 | Implementado | Sí: contención de una misma butaca | Sí: libre, retenida y ocupada | Sí mediante servicios | Pendiente: mapa visual y dos navegadores | Desconocido |
| RF-M5-01 | Implementado | Sí: vencimiento, cancelación e idempotencia | Sí | Sí | Pendiente: temporizador y liberación visibles | Desconocido |
| RF-M5-02 | Implementado | Sí: búsqueda focal por código 1/1 | Sí: búsqueda y validación | Sí | Pendiente en pantalla de ventas/pago | Desconocido |
| RF-M5-03 | Implementado | Sí: emisión, concurrencia, rollback y liberación | Sí: emisión pagada/gratuita, cancelación exacta y QR | Sí | Pendiente: QR, historial y cancelación visual | Desconocido |
| RF-M6-02 | Implementado | Lógica unitaria aprobada; DAO/MySQL aislado desconocido | Sí: vigentes, gratuitas y canceladas | Sí | Pendiente: pantalla y totales | Desconocido |
| RF-M6-03 | Implementado | Filtros unitarios aprobados; DAO/MySQL aislado desconocido | Sí: ingresos, emitidas, canceladas y filtros | Sí | Pendiente: filtros, vacío y totales | Desconocido |
| RF-M6-04 | Implementado | Negocio unitario aprobado; DAO/MySQL aislado desconocido | Sí: alta y edición persistente | Sí | Pendiente: alta, edición y consulta visual | Desconocido |

### Artefactos vigentes y entrega local

- No hay cambios de código productivo posteriores a los artefactos. El `git pull` posterior actualizó fechas de archivos, pero el contenido productivo coincide con `HEAD`; la única diferencia local del frontend es `appsettings.json`, y su copia en `publish` coincide por SHA-256. Por ello no se recompiló.
- **WAR vigente:** `Backend/entrefunciones-rest/target/entrefunciones-rest.war`, 14 275 142 bytes, 2026-11-15 23:25:03 -05:00, SHA-256 `0041FB8525620B7BEBC9B89C2E911565BD414AC5086BD9B413CE2098952EE34C`.
- **Frontend vigente:** `Frontend/EntreFunciones/bin/Release/net10.0/publish/`; `EntreFunciones.dll`, 574 976 bytes, 2026-11-15 22:42:52 -05:00, SHA-256 `979436242619A4481026870DE181C3F5EC88C6F2CFBB1BB7944B61AB3B651B94`.
- **Nueva carpeta preparada:** `Entrega-AWS-Lab12/`, con `Backend/entrefunciones-rest.war` y el contenido completo de `publish` bajo `Frontend/`. No se utilizó `Entrega-AWS` como origen.
- **Verificación de copia:** 45/45 archivos de producto coinciden con sus originales en tamaño y SHA-256; el manifiesto completo está en `Entrega-AWS-Lab12/MANIFIESTO-SHA256.md`.
- **URL REST incluida:** `http://ec2-3-89-249-99.compute-1.amazonaws.com:8080/entrefunciones-rest/v1/api/`. `web.config` no fija `Rest__BaseUrl`; puede sobrescribirse externamente durante la ejecución.
- No se añadieron claves JWT, contraseñas, tokens ni credenciales al paquete, al manifiesto o a esta carpeta de contexto.

### Pendientes posteriores al despliegue

1. Confirmar por navegador login válido e inválido, redirección por rol, cookie y cierre de sesión.
2. Verificar CRUD visual de sedes, salas, butacas y bloqueos, incluida la conservación histórica.
3. Programar, confirmar, publicar y cancelar funciones; comprobar mensajes ante bloqueos y cruces.
4. Probar cartelera y mapa de butacas con filtros combinados, retención y dos sesiones concurrentes.
5. Completar visualmente reserva, pago, emisión pagada/gratuita, QR, cancelación individual y liberación exacta.
6. Revisar ambos reportes e incidencias con datos pagados, gratuitos y cancelados, incluido un conjunto sin resultados.
7. Verificar en AWS los artefactos, el esquema y la configuración efectivos. Esta acción requiere autorización de despliegue y no se realizó.

El siguiente paso técnico es desplegar, con autorización expresa, los artefactos de `Entrega-AWS-Lab12` y ejecutar estas comprobaciones de navegador. RF-M1-03 permanece pendiente de implementación.

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
