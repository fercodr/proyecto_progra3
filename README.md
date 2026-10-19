# EntreFunciones

Proyecto Java multimodulo administrado con Maven.

## Estructura

- `Codigo/`: proyecto Maven y scripts SQL.
- `docs/entregas/`: documentos finales de cada entrega.
- `entrega.bat`: genera el ZIP academico sin copiar archivos dentro del repositorio.

## Abrir en IntelliJ IDEA

1. En IntelliJ, selecciona **File > Open**.
2. Selecciona `Codigo/pom.xml` y abrelo como proyecto.
3. Si IntelliJ pregunta si deseas cargar Maven, selecciona **Load Maven Project**.
4. En **File > Project Structure > Project**, selecciona el JDK 25 instalado
   como **Project SDK**.
5. En **Settings > Build, Execution, Deployment > Build Tools > Maven**,
   selecciona **Use Maven wrapper** en **Maven home path**.
6. Espera a que termine la sincronizacion de Maven.

El codigo se compila con compatibilidad Java 21. No necesitas instalar Maven de
forma global: el repositorio incluye Maven Wrapper.

## Compilar desde la terminal

Desde la carpeta `Codigo`:

```powershell
.\mvnw.cmd clean verify
```

En macOS o Linux:

```bash
./mvnw clean verify
```

La primera ejecucion descarga Maven y las dependencias, por lo que requiere
conexion a Internet.

## Ejecutar la aplicacion

En IntelliJ, abre
`entrefunciones-app/src/main/java/pe/edu/pucp/entrefunciones/app/Main.java`
y ejecuta el metodo `main` con el boton verde del editor.

## Generar la entrega

Desde la raiz del repositorio, ejecuta:

```powershell
.\entrega.bat
```

Sin argumentos prepara `LAB06`. Para otra carpeta existente en
`docs/entregas/`, indica su nombre:

```powershell
.\entrega.bat LAB07
```

El resultado queda como `EntreFunciones_<ENTREGA>.zip` en la raiz. El paquete
incluye `Documentos/` y `Codigo/`, pero excluye metadatos Git e IntelliJ,
`target`, archivos `.codex-*`, configuraciones privadas, credenciales y
temporales.
