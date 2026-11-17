# Configuracion compartida

`db.properties` es la única fuente versionada de configuración compartida del
backend. Maven la toma desde esta carpeta al compilar
`entrefunciones-dbmanager` y la incorpora al classpath; no debe copiarse a los
directorios `resources` de otros módulos.

El archivo solo conserva valores no sensibles. Cada entorno reemplaza la
configuración mediante `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` y
`DB_PASSWORD`; la autenticación REST recibe `ENTREFUNCIONES_JWT_SECRET`. Los
campos de usuario, contraseña y secreto permanecen vacíos en Git.

La URL REST del frontend se configura por separado con `Rest__BaseUrl`. Consulta
la [guía de acceso y despliegue](../docs/GUIA-ACCESO.md).
