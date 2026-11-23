# Configuracion compartida

`db.properties` es la única fuente versionada de configuración compartida del
backend. Maven la toma desde esta carpeta al compilar
`entrefunciones-dbmanager` y la incorpora al classpath; no debe copiarse a los
directorios `resources` de otros módulos.

El archivo solo conserva valores no sensibles. Cada entorno reemplaza la
configuración mediante `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` y
`DB_PASSWORD`; la autenticación REST recibe `ENTREFUNCIONES_JWT_SECRET`. Los
campos de usuario, contraseña y secreto permanecen vacíos en Git.

El almacenamiento de archivos configura `S3_BUCKET`, `S3_REGION`,
`AWS_ACCESS_KEY_ID` y `AWS_SECRET_ACCESS_KEY`. Si se usan credenciales
temporales, también requiere `AWS_SESSION_TOKEN`. Los valores reales y el
archivo local `s3.properties` no se versionan.

La URL REST del frontend se configura por separado con `Rest__BaseUrl`. Consulta
la [guía de acceso y despliegue](../docs/GUIA-ACCESO.md).
