# Reportes PDF con JasperReports

## Alcance

La pantalla `/admin` conserva la política Blazor `Reportes` y los endpoints PDF
vuelven a exigir el rol REST `Administrador`. Ambos documentos usan
`FuncionBL.findReportes(...)`, la misma consulta que alimenta las tablas; no
replican SQL ni recalculan cifras en el frontend.

Los endpoints son:

- `GET /funciones/reportes/ocupacion/pdf`
  - parámetros opcionales: `desde`, `hasta`, `sedeId`, `salaId`;
- `GET /funciones/reportes/ingresos/pdf`
  - parámetros opcionales: `desde`, `hasta`, `sedeId`, `tipoEvento`.

Las fechas usan `yyyy-MM-dd`. Una fecha omitida no se envía y significa límite
abierto. La respuesta correcta es `200 application/pdf`, con
`Content-Disposition: attachment`; un usuario sin rol administrador recibe
`403`.

## Prueba desde el navegador

Requisitos: un GlassFish local autorizado, un esquema de pruebas con los
procedimientos vigentes y una cuenta de prueba con rol administrador. No use la
base compartida ni AWS para esta comprobación local.

1. Construya el WAR desde `Backend`:

   ```powershell
   .\mvnw.cmd -pl entrefunciones-rest -am package
   ```

2. Despliegue localmente
   `Backend/entrefunciones-rest/target/entrefunciones-rest.war` en el GlassFish
   aislado y configure el frontend para apuntar a ese REST.
3. Inicie el frontend desde la raíz del repositorio:

   ```powershell
   $env:Rest__BaseUrl = "http://127.0.0.1:PUERTO/entrefunciones-rest/v1/api/"
   dotnet run --project Frontend/EntreFunciones/EntreFunciones.csproj
   ```

4. Inicie sesión manualmente como administrador y abra `/admin`.
5. En **Ocupación por entradas**, pulse **Consultar** y luego
   **Descargar PDF**. Compruebe función, fecha, sala, aforo, entradas vigentes y
   porcentaje de ocupación.
6. En **Ingresos y cancelaciones**, repita la descarga y compruebe función,
   fecha, ingresos en soles, entradas emitidas y canceladas.

El botón vuelve a consultar antes de descargar. Así, la tabla visible y el PDF
usan los mismos filtros aunque el usuario haya cambiado un control después de
la consulta anterior.

## Casos de aceptación

| Caso | Filtros | Resultado esperado |
|---|---|---|
| Sin fechas | Dejar `Desde` y `Hasta` vacíos | Tabla y PDF consultan todo el período; el encabezado dice `Periodo: todo`. |
| Ocupación filtrada | Período con datos + sede + sala | Cada fila y cifra del PDF coincide con la tabla. |
| Ingresos filtrados | Período con datos + sede + tipo de actividad | Ingresos, emitidas y canceladas coinciden con la tabla. |
| Sin coincidencias | Período o combinación sin funciones | Tabla vacía; PDF válido con título, filtros y mensaje `No se encontraron resultados`. |

Para comparar cifras, use una misma fila identificada por título y fecha. La
ocupación debe conservar dos decimales; los ingresos deben conservar dos
decimales y pueden incluir separador de miles.

## Verificación realizada en esta rama

- `dotnet build --no-restore`: aprobado, 0 advertencias y 0 errores.
- `mvnw -pl entrefunciones-rest -am test`: aprobado.
  - negocio: 32 pruebas aprobadas y 18 omitidas por configuración de integración;
  - REST: 3 pruebas Jasper aprobadas y 3 pruebas de integración omitidas.
- Las pruebas Jasper generan PDF reales y vuelven a abrir los bytes exportados.
  Comprueban títulos, filtros, cifras, encabezado `%PDF` y ambos documentos sin
  resultados.
- El WAR local se generó correctamente y contiene las plantillas JRXML y las
  dependencias JasperReports/OpenPDF.

No se ejecutó el recorrido navegador → Blazor → REST → MySQL. El puerto local
`8080` estaba ocupado por un cliente de túnel y no se consultó para evitar
alcanzar AWS. Tampoco se dispuso de un esquema local aislado y autorizado con
cuenta administrativa. Por lo tanto quedan pendientes:

- la aceptación visual en navegador con datos reales de prueba;
- la comparación tabla/PDF contra MySQL mediante HTTP;
- autorización 200/403 comprobada sobre un servidor desplegado;
- despliegue y smoke test en AWS, a cargo del equipo.

No se modificaron AWS, GlassFish, bases de datos, credenciales ni túneles durante
esta implementación.
