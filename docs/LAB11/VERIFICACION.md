# Verificación de las correcciones

## Comprobaciones ejecutadas

| Comprobación | Resultado |
|---|---|
| Compilación de todos los fuentes Java, incluido REST | Correcta con Java 21 |
| Pruebas JUnit | 24 ejecutadas, 24 correctas |
| Compilación de C# y Razor con .NET 10.0.403 | Correcta, cero errores y cero advertencias |
| Instalación del esquema, 68 procedimientos y datos opcionales | Correcta en MariaDB 10.11 aislado |
| Migración desde el SQL del ZIP original | Conserva las 4 reservas y 4 entradas de ejemplo; elimina las dos entidades retiradas |
| Migración con la tabla puente usando el nombre antiguo | Recupera el nombre canónico y conserva las referencias |
| Auditoría estática de llamadas a procedimientos | Sin nombres ausentes ni diferencias de cantidad de argumentos en las llamadas literales |
| Simulación del modelo en Main | 28 de 28 verificaciones del dominio correctas |
| Informe Word | Texto, matriz, fichas y diagramas actualizados y revisados mediante renderizado |

Las pruebas incluyen ocho casos existentes de Espacios, tres de transacciones con H2 y trece nuevos de integración contra MariaDB utilizando el driver MySQL del proyecto. Verifican reservas numeradas, aforo general, precio persistido, butacas duplicadas o ajenas, concurrencia sobre butacas y cupos, expiración, cancelación, emisión sin butaca en aforo general, pago repetido, rollback de toda la validación cuando falla una emisión, cruces y concurrencia de horarios, y reconstrucción de consultas DAO.

Las pruebas de integración solo se habilitan con `EF_TEST_DB=1` y exigen que `DB_NAME` termine en `_test`; necesitan previamente el esquema, procedimientos y datos opcionales en esa BD aislada. Su preparación elimina datos de esa base de pruebas. Las pruebas normales de Espacios y H2 siguen disponibles sin ese indicador.

## Límite de la verificación

No se ejecutó el despliegue final de GlassFish ni una sesión completa de navegador con ambos servidores. Tampoco se aplicó la migración a la BD compartida del equipo. La compatibilidad con el MySQL concreto de AWS y el temporizador EJB deben comprobarse allí al integrar. Se compiló Java y se ejecutó JUnit directamente; el empaquetado Maven completo no quedó verificado en este entorno.

El frontend compilado incluye las tres páginas conectadas, pero el recorrido desde Cartelera depende de sustituir los IDs demo por los reales. El login demo tampoco se considera autenticación real ni un RF terminado.
