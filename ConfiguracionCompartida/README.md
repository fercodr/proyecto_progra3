# Configuracion compartida

`db.properties` es la configuracion comun de la base de datos del equipo.

## Integracion con Backend y REST

No es necesario copiar el archivo manualmente dentro de REST. El modulo
`entrefunciones-dbmanager` lo toma desde esta carpeta durante la compilacion y
lo coloca en la raiz de su JAR. REST depende de `entrefunciones-negocio`, que a
su vez usa acceso a datos y `entrefunciones-dbmanager`; por eso el recurso queda
disponible en el classpath del WAR como `db.properties`.

Para generar todos los artefactos, ejecuta Maven desde `Backend`:

```powershell
mvn clean package
```

`DBManager` permite reemplazar cada valor sin modificar el archivo mediante
las variables `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` y `DB_PASSWORD`.

La autenticacion REST tambien requiere `ENTREFUNCIONES_JWT_SECRET` con al menos
32 bytes. Esa variable es independiente de la conexion a la base de datos.

La URL que usa el Frontend para llamar a REST se configura por separado en
`Frontend/EntreFunciones/appsettings.json` mediante `Rest:BaseUrl`.
