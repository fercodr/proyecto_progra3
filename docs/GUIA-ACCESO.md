# Guía de acceso y configuración

## Direcciones verificadas

| Servicio | Dirección | Uso |
|---|---|---|
| Frontend público | `http://18.206.246.134/` | Acceso de usuarios desde Internet. |
| Backend público | `http://98.81.195.228:8080/entrefunciones-rest/v1/api/` | Diagnóstico o consumo externo autorizado. |
| Backend privado | `http://172.31.21.178:8080/entrefunciones-rest/v1/api/` | Comunicación interna desde recursos de la misma red privada de AWS. |

## Acceso web

La aplicación se abre desde Internet en `http://18.206.246.134/`. La API
pública se encuentra en
`http://98.81.195.228:8080/entrefunciones-rest/v1/api/`. El acceso a la API no
sustituye la interfaz web y debe respetar la autenticación y los roles del
sistema.

## Escritorio remoto

El host del frontend también es el destino administrativo para Conexión a
Escritorio remoto de Windows (`mstsc`):

```text
18.206.246.134
```

El puerto permitido, el usuario y la contraseña se obtienen por el canal
seguro acordado por el equipo. No deben guardarse en el repositorio, en la guía
ni en capturas o registros de consola.

## Configuración interna de AWS

El frontend desplegado en AWS debe recibir la URL privada del backend mediante
la variable de entorno de .NET:

```text
Rest__BaseUrl=http://172.31.21.178:8080/entrefunciones-rest/v1/api/
```

La IP privada no es una dirección para equipos fuera de la VPC. La URL pública
del backend se conserva para acceso externo autorizado y comprobaciones
operativas, no como valor predeterminado del código fuente.

Las variables `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` y
`ENTREFUNCIONES_JWT_SECRET` se configuran en el entorno de ejecución. Sus
valores no deben almacenarse en Git ni escribirse en registros.

## Desarrollo local y desde casa

`Frontend/EntreFunciones/appsettings.json` conserva como valor predeterminado:

```text
http://localhost:8080/entrefunciones-rest/v1/api/
```

Esto supone un backend local en el puerto `8080`. Para desarrollar desde casa
contra el backend remoto autorizado, se puede definir sin modificar el archivo
versionado:

```text
Rest__BaseUrl=http://98.81.195.228:8080/entrefunciones-rest/v1/api/
```

La dirección privada `172.31.21.178` se reserva para la red interna de AWS y
no debe usarse desde una conexión doméstica. Para cualquier otro endpoint se
usa `Rest__BaseUrl` o una configuración local ignorada por Git. La conexión de
base de datos del backend se reemplaza con las variables `DB_*`; no se cambia
`DB_NAME` en archivos versionados para alternar entornos ni se expone la base
de datos directamente a Internet.
