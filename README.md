# EntreFunciones

Aplicación de Programación 3 para administrar funciones, reservas, pagos,
entradas, reportes e incidencias. El backend es Java/Jakarta EE y se despliega
como WAR; el frontend es una aplicación Blazor Server sobre .NET 10.

## Inicio rápido

1. Configura el backend mediante `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`,
   `DB_PASSWORD` y `ENTREFUNCIONES_JWT_SECRET`. No guardes valores sensibles
   en archivos versionados.
2. Compila el backend desde `Backend`:

   ```powershell
   .\mvnw.cmd package
   ```

3. Ejecuta el frontend desde la raíz:

   ```powershell
   dotnet run --project Frontend/EntreFunciones/EntreFunciones.csproj
   ```

El frontend local usa por defecto el REST en `localhost:8080`. Cualquier URL
de otro entorno se proporciona mediante `Rest__BaseUrl`, sin modificar el
archivo versionado.

## Documentación

- [Catálogo funcional vigente](docs/CATALOGO.md)
- [Estado, verificaciones y pendientes](docs/ESTADO.md)
- [Acceso, configuración y despliegue en AWS](docs/GUIA-ACCESO.md)
- [Reportes, incidencias y migraciones relacionadas](docs/reportes-incidencias.md)
- [Catálogo técnico de procedimientos almacenados](docs/catalogo-sp.md)
- [Entregas históricas conservadas](docs/entregas/)
- [Configuración compartida del backend](ConfiguracionCompartida/README.md)

## Estructura principal

- `Backend/`: proyecto Maven, código Java, REST y scripts SQL.
- `Frontend/`: aplicación Blazor Server.
- `ConfiguracionCompartida/`: única fuente versionada de la configuración no
  sensible que Maven incorpora al backend.
- `docs/`: documentación funcional, operativa, técnica e histórica.
